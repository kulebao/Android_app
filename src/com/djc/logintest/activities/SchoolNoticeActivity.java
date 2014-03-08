package com.djc.logintest.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.djc.logintest.R;
import com.djc.logintest.adapter.MyGridViewAdapter;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.ChildInfo;
import com.djc.logintest.dbmgr.info.InfoHelper;
import com.djc.logintest.dlgmgr.DlgMgr;
import com.djc.logintest.handler.MyHandler;
import com.djc.logintest.handler.TaskResultHandler;
import com.djc.logintest.receiver.NotificationObserver;
import com.djc.logintest.taskmgr.CheckChildrenInfoTask;
import com.djc.logintest.taskmgr.DownLoadImgAndSaveTask;
import com.djc.logintest.taskmgr.UploadInfoTask;
import com.djc.logintest.threadpool.MyThreadPoolMgr;
import com.djc.logintest.upload.OSSMgr;
import com.djc.logintest.utils.MethodUtils;
import com.djc.logintest.utils.Utils;

public class SchoolNoticeActivity extends TabChildActivity {
	private GridView gridview;
	public static final int NORMAL_NOTICE = 0;
	public static final int COOK_NOTICE = 1;
	public static final int SWAPCARD_NOTICE = 2;
	public static final int SCHEDULE = 3;
	public static final int HOMEWORK = 4;
	public static final int INTERACTION = 5;
	public static final int EDUCATION = 6;

	// 下列序号与资源文件arrays.xml中的baby_setting_items配置保持一致
	private static final int SET_BABY_NICKNAME = 0;
	private static final int SET_BABY_BIRTHDAY = 1;
	private static final int SET_BABY_ICON_BY_TAKE_PHOTO = 2;
	private static final int SET_BABY_ICON_FROM_ICONLIB = 3;
	private static final long ONE_YEAR = 24 * 365 * 60 * 60 * 1000L;
	private static final int IMAGE_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	private static final int RESIZE_REQUEST_CODE = 2;
	private static final String IMAGE_FILE_NAME = "header.jpg";
	private static String PHOTO = "icon_url";
	private static String BIRTHDAY = "birthday";
	private static String NICK = "nick";
	private ImageView babyHeadIcon;
	private ChildInfo selectedChild;
	private AsyncTask<Void, Void, Integer> downloadIconTask;
	private Handler handler;
	private ProgressDialog progressDialog;
	private MyGridViewAdapter adapter;
	private NotificationObserver notificationObserver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.school_notice);
		initProgressDlg();
		initHandler();
		initUI();
		// 检查小孩信息是否有更新，有更新需要及时更新
		runCheckChildrenInfoTask();
		registObserver();
		checkNewDatas();
	}

	private void checkNewDatas() {
		MethodUtils.executeCheckNewsCommand(this);
		MethodUtils.executeCheckHomeworkCommand(this);
		MethodUtils.executeCheckCookbookCommand(this);
		MethodUtils.executeCheckScheduleCommand(this);
	}

	private void registObserver() {
		notificationObserver = new NotificationObserver() {
			@Override
			public void update(int noticeType, int param) {
				adapter.notifyDataSetChanged();
			}
		};
		MyApplication.getInstance().addObserver(notificationObserver);
	}

	private void unRegistObserver() {
		MyApplication.getInstance().addObserver(notificationObserver);
	}

	public void initUI() {
		initGridView();
		initBtn();
		initTitle();
		setTabTitle();
		setSchoolName();
	}

	private void initProgressDlg() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
	}

	private void runCheckChildrenInfoTask() {
		new CheckChildrenInfoTask(handler).execute();
	}

	private void initHandler() {
		handler = new MyHandler(this, progressDialog) {
			@Override
			public void handleMessage(Message msg) {
				if (SchoolNoticeActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.UPDATE_CHILDREN_INFO:
					setTabTitle();
					setClassName();
					break;
				case EventType.DOWNLOAD_IMG_SUCCESS:
					handleDownloadImgSuccess((String) msg.obj);
					break;
				case EventType.SERVER_INNER_ERROR:
					Toast.makeText(SchoolNoticeActivity.this,
							R.string.get_child_info_fail, Toast.LENGTH_SHORT)
							.show();
					break;
				default:
					break;
				}
			}

		};
	}

	public void handleDownloadImgSuccess(String filepath) {
		DataMgr instance = DataMgr.getInstance();
		instance.updateChildLocalIconUrl(instance.getSelectedChild()
				.getServer_id(), filepath);
		// 本地文件保存更新,在之前如果为空的情况下，需要重新设置
		selectedChild.setLocal_url(filepath);
		Bitmap loacalBitmap = Utils.getLoacalBitmap(filepath);
		Utils.setImg(babyHeadIcon, loacalBitmap);
	}

	protected void setTabTitle() {
		selectedChild = DataMgr.getInstance().getSelectedChild();
		if (selectedChild != null) {
			setNickName();
			setBirthDay();
			setHeadIcon();
			setClassName();
		}
	}

	public void setClassName() {
		TextView classnameView = (TextView) findViewById(R.id.classname);
		classnameView.setText(DataMgr.getInstance().getSelectedChild()
				.getClass_name());
	}

	public void setSchoolName() {
		TextView schoolNameView = (TextView) findViewById(R.id.schoolName);
		schoolNameView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startToSchoolInfoActivity();
			}
		});
		schoolNameView.setText(DataMgr.getInstance().getSchoolInfo()
				.getSchool_name());
	}

	private void setHeadIcon() {
		String url = selectedChild.getLocal_url();
		if (!"".equals(url)) {
			Bitmap loacalBitmap = Utils.getLoacalBitmap(url);
			Utils.setImg(babyHeadIcon, loacalBitmap);
		} else if (!"".equals(selectedChild.getServer_url())) {

			if (downloadIconTask != null
					&& downloadIconTask.getStatus() == AsyncTask.Status.RUNNING) {
				// 后执行的取消先执行的
				downloadIconTask.cancel(true);
			}

			downloadIconTask = new DownLoadImgAndSaveTask(handler,
					selectedChild.getServer_url(),
					InfoHelper.getChildrenLocalIconPath(selectedChild
							.getServer_id())).execute();
		}
	}

	private void setBirthDay() {
		Long birthDay = Long.parseLong(selectedChild.getChild_birthday());
		String age = getAge(birthDay);
		if (!"".equals(age)) {
			TextView birthdayView = (TextView) findViewById(R.id.child_age);
			birthdayView.setText(age);
		}
	}

	private String getAge(Long time) {
		StringBuffer age = new StringBuffer();
		Calendar birthDay = Calendar.getInstance();

		// 当前时间一定要比生日大1岁,否则可能是用户手机时间不对
		if (birthDay.getTimeInMillis() > ONE_YEAR) {
			birthDay.setTimeInMillis(time);

			Calendar current = Calendar.getInstance();

			int year = current.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);
			int month = current.get(Calendar.MONTH)
					- birthDay.get(Calendar.MONTH);

			if (month > 0) {
				age.append(String.format(
						getResources().getString(R.string.age_year), year));
				age.append(String.format(
						getResources().getString(R.string.age_month), month));
			} else if (month == 0) {
				age.append(String.format(
						getResources().getString(R.string.age_year), year));
			} else {
				year = year - 1;
				month = 12 + month;
				age.append(String.format(
						getResources().getString(R.string.age_year), year));
				age.append(String.format(
						getResources().getString(R.string.age_month), month));
			}

		}

		return age.toString();
	}

	private void setBabyHeadIconFromIconLib() {
		Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
		galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
		galleryIntent.setType("image/*");
		startActivityForResult(galleryIntent, IMAGE_REQUEST_CODE);
	}

	private void setBabyHeadIconByTakePhoto() {
		if (Utils.isSdcardExisting()) {
			Intent cameraIntent = new Intent(
					"android.media.action.IMAGE_CAPTURE");
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, getImageUri());
			cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
			startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
		} else {
			Toast.makeText(this, "请插入sd卡", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	// 处理从图库和拍照返回的照片
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ConstantValue.START_SETTING
				&& resultCode == ConstantValue.EXIT_LOGIN_RESULT) {
			Intent intent = new Intent();
			intent.setClass(this, ValidatePhoneNumActivity.class);
			startActivity(intent);
			finish();
			return;
		}

		if (resultCode != RESULT_OK) {
			// Toast.makeText(this, "头像选择失败，错误码:" + resultCode,
			// Toast.LENGTH_SHORT).show();
			return;
		} else {
			switch (requestCode) {
			case IMAGE_REQUEST_CODE:
				resizeImage(data.getData());
				break;
			case CAMERA_REQUEST_CODE:
				if (Utils.isSdcardExisting()) {
					resizeImage(getImageUri());
				} else {
					Toast.makeText(SchoolNoticeActivity.this, "未找到存储卡，无法存储照片！",
							Toast.LENGTH_LONG).show();
				}
				break;

			case RESIZE_REQUEST_CODE:
				uploadIcon(data);
				break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void uploadIcon(Intent data) {
		if (data != null) {
			try {
				Bitmap bitmap = getBitmap(data);
				JSONObject obj = new JSONObject();
				obj.put(PHOTO, OSSMgr.OSS_HOST + Utils.getUpload2OssChildUrl());

				runUploadTask(obj.toString(), bitmap,
						getResources().getString(R.string.uploading_icon),
						new TaskResultHandler() {
							@Override
							public void handleResult(int result, Object param) {
								progressDialog.cancel();
								if (result == EventType.UPLOAD_SUCCESS) {
									updateChildPhoto((Bitmap) param);
								} else {
									Toast.makeText(SchoolNoticeActivity.this,
											R.string.upload_icon_failed,
											Toast.LENGTH_SHORT).show();
								}
							}
						});
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	// 此时是选中或拍好照片后，对照片进行裁剪
	public void resizeImage(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 100);
		intent.putExtra("outputY", 100);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, RESIZE_REQUEST_CODE);
	}

	private Bitmap getBitmap(Intent data) {
		Bundle extras = data.getExtras();
		Bitmap photo = null;
		if (extras != null) {
			photo = extras.getParcelable("data");
		}
		return photo;
	}

	private void runUploadTask(String content, Bitmap bitmap, String notice,
			TaskResultHandler task) {
		UploadInfoTask uploadIconTask = new UploadInfoTask(task, content);

		if (bitmap != null) {
			uploadIconTask.setBitmap(bitmap);
		}

		progressDialog.setMessage(notice);
		progressDialog.show();
		uploadIconTask.execute();
	}

	public void updateChildPhoto(Bitmap photo) {
		try {
			Utils.setImg(babyHeadIcon, photo);
			String path = InfoHelper.getChildrenLocalIconPath(selectedChild
					.getServer_id());
			Utils.saveBitmapToSDCard(photo, path);
			DataMgr.getInstance().updateChildLocalIconUrl(
					selectedChild.getServer_id(), path);
			selectedChild.setLocal_url(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Uri getImageUri() {
		return Uri.fromFile(new File(Utils.getSDCardFileDir(Utils.APP_DIR_TMP),
				IMAGE_FILE_NAME));
	}

	private void setNickName() {
		TextView nicknamView = (TextView) findViewById(R.id.child_name);
		nicknamView.setText(selectedChild.getChild_nick_name());
	}

	@Override
	protected void onResume() {
		super.onResume();
		adapter.notifyDataSetChanged();
		if (selectedChild != null
				&& selectedChild.getId() != DataMgr.getInstance()
						.getSelectedChild().getId()) {
			Log.d("DDD", "selectedChild changed redraw!");
			setTabTitle();
		}
		// 查看学校信息后，学校信息可能会变化，这里更新ui
		setSchoolName();
	}

	private void initTitle() {
		babyHeadIcon = (ImageView) findViewById(R.id.child_photo);
		babyHeadIcon
				.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!Utils
								.isNetworkConnected(SchoolNoticeActivity.this)) {
							Toast.makeText(SchoolNoticeActivity.this,
									R.string.net_error, Toast.LENGTH_SHORT)
									.show();
							return;
						} else if (selectedChild == null) {
							Toast.makeText(SchoolNoticeActivity.this,
									R.string.reget_child_info,
									Toast.LENGTH_SHORT).show();
							return;
						}
						DlgMgr.getListDialog(SchoolNoticeActivity.this,
								R.array.baby_setting_items,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										Log.d("initTitle ddd", "which ="
												+ which);
										handleClick(which);
									}
								}).create().show();
					}
				});
	}

	private void handleClick(int which) {
		switch (which) {
		case SET_BABY_NICKNAME:
			showTextDlg();
			break;
		case SET_BABY_BIRTHDAY:
			showDateDlg();
			break;
		case SET_BABY_ICON_FROM_ICONLIB:
			setBabyHeadIconFromIconLib();
			break;
		case SET_BABY_ICON_BY_TAKE_PHOTO:
			setBabyHeadIconByTakePhoto();
			break;

		default:
			break;
		}
	}

	public void uploadBirthday(Calendar birthday) {
		final long born = birthday.getTimeInMillis();

		try {
			JSONObject obj;
			obj = new JSONObject();
			obj.put(BIRTHDAY, born);

			runUploadTask(obj.toString(), null,
					getResources().getString(R.string.uploading_child_info),
					new TaskResultHandler() {
						@Override
						public void handleResult(int result, Object param) {
							progressDialog.cancel();
							if (result == EventType.UPLOAD_SUCCESS) {
								DataMgr.getInstance().updateBirthday(
										selectedChild.getServer_id(), born);
								selectedChild.setChild_birthday(String
										.valueOf(born));
								setBirthDay();
							} else {
								Toast.makeText(SchoolNoticeActivity.this,
										R.string.uploading_child_info_failed,
										Toast.LENGTH_SHORT).show();
							}
						}
					});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void showDateDlg() {
		Calendar calendar = Calendar.getInstance();
		OnDateSetListener dateListener = new OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				Calendar birthday = Calendar.getInstance();
				birthday.set(year, monthOfYear, dayOfMonth);
				Log.d("", "date :" + birthday.toString());

				if (!checkValid(birthday)) {
					Toast.makeText(SchoolNoticeActivity.this,
							R.string.baby_age_limit, Toast.LENGTH_SHORT).show();
					return;
				}

				uploadBirthday(birthday);
			}

		};

		// 把日期控件的初始时间设置为小孩当前生日
		Long birthDay = Long.parseLong(selectedChild.getChild_birthday());
		calendar.setTimeInMillis(birthDay);

		DatePickerDialog dialog = new DatePickerDialog(this, dateListener,
				calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH));
		dialog.setTitle(R.string.set_birthday);
		dialog.show();
	}

	protected boolean checkValid(Calendar birthday) {
		Calendar current = Calendar.getInstance();
		long time = current.getTimeInMillis() - birthday.getTimeInMillis();
		// 幼儿年龄必须超过1岁
		if (time > ONE_YEAR) {
			return true;
		}
		return false;
	}

	// 修改昵称的对话框
	private void showTextDlg() {
		final View textEntryView = LayoutInflater.from(this).inflate(
				R.layout.baby_nickname_text_entry, null);
		final EditText nicknameEdit = (EditText) textEntryView
				.findViewById(R.id.baby_nickname_edit);
		new AlertDialog.Builder(this)
				.setTitle(R.string.set_baby_nickname)
				.setView(textEntryView)
				.setPositiveButton(R.string.confirm,
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								final String nick = nicknameEdit.getText()
										.toString();
								uploadNick(nick);
							}

						}).setNegativeButton(R.string.back, null).create()
				.show();
	}

	public void uploadNick(final String nick) {
		try {
			JSONObject obj;
			obj = new JSONObject();
			obj.put(NICK, nick);

			runUploadTask(obj.toString(), null,
					getResources().getString(R.string.uploading_child_info),
					new TaskResultHandler() {
						@Override
						public void handleResult(int result, Object param) {
							progressDialog.cancel();
							if (result == EventType.UPLOAD_SUCCESS) {
								DataMgr.getInstance().updateNick(
										selectedChild.getServer_id(), nick);
								selectedChild.setChild_nick_name(nick);
								setNickName();
							} else {
								Toast.makeText(SchoolNoticeActivity.this,
										R.string.uploading_child_info_failed,
										Toast.LENGTH_SHORT).show();
							}
						}
					});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void initBtn() {
		ImageView settingBtn = (ImageView) findViewById(R.id.settingBtn);
		settingBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startToSettingActivity();
			}
		});
	}

	public void initGridView() {
		gridview = (GridView) findViewById(R.id.gridview);
		ArrayList<HashMap<String, Object>> lstImageItem = initData();
		adapter = new MyGridViewAdapter(this, lstImageItem);
		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(new ItemClickListener());
	}

	public ArrayList<HashMap<String, Object>> initData() {
		ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("ItemImage", R.drawable.pnotice);
		map.put("ItemText", getResources().getText(R.string.pnotice));
		lstImageItem.add(map);

		map = new HashMap<String, Object>();
		map.put("ItemImage", R.drawable.cook);
		map.put("ItemText", getResources().getText(R.string.cook));
		lstImageItem.add(map);

		map = new HashMap<String, Object>();
		map.put("ItemImage", R.drawable.swap);
		map.put("ItemText", getResources().getText(R.string.swap));
		lstImageItem.add(map);

		map = new HashMap<String, Object>();
		map.put("ItemImage", R.drawable.schedule);
		map.put("ItemText", getResources().getText(R.string.schedule));
		lstImageItem.add(map);

		map = new HashMap<String, Object>();
		map.put("ItemImage", R.drawable.schedule);
		map.put("ItemText", getResources().getText(R.string.homework));
		lstImageItem.add(map);

		map = new HashMap<String, Object>();
		map.put("ItemImage", R.drawable.schedule);
		map.put("ItemText", getResources().getText(R.string.interaction));
		lstImageItem.add(map);

		map = new HashMap<String, Object>();
		map.put("ItemImage", R.drawable.schedule);
		map.put("ItemText", getResources().getText(R.string.education));
		lstImageItem.add(map);

		return lstImageItem;
	}

	// 当AdapterView被单击(触摸屏或者键盘)，则返回的Item单击事件
	private class ItemClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			handleGridViewClick(position);
		}
	}

	public void handleGridViewClick(int position) {
		// 未获取到小孩信息时，不能响应按键
		if (selectedChild == null) {
			Toast.makeText(SchoolNoticeActivity.this,
					R.string.reget_child_info, Toast.LENGTH_SHORT).show();
			return;
		}

		switch (position) {
		case COOK_NOTICE:
			startToCookbookActivity();
			break;
		case SWAPCARD_NOTICE:
			startToSwipeCalendarActivity();
			break;
		case NORMAL_NOTICE:
			startToNoticeRecordActivity(JSONConstant.NOTICE_TYPE_NORMAL);
			break;
		case SCHEDULE:
			startToScheduleActivity();
			break;
		case HOMEWORK:
			startToHomeworkActivity();
			break;
		case INTERACTION:
			startToInteractionActivity();
			break;
		case EDUCATION:
			startToEducationActivity();
			break;

		default:
			Toast.makeText(this, "暂未实现！", Toast.LENGTH_SHORT).show();
			break;
		}
	}

	private void startToEducationActivity() {
		Intent intent = new Intent();
		intent.setClass(this, EducationActivity.class);
		startActivity(intent);	
	}

	private void startToInteractionActivity() {
		Intent intent = new Intent();
		intent.setClass(this, InteractionActivity.class);
		startActivity(intent);
	}

	private void startToSettingActivity() {
		Intent intent = new Intent();
		intent.setClass(this, SettingActivity.class);
		startActivityForResult(intent, ConstantValue.START_SETTING);
	}

	private void startToNoticeRecordActivity(int noticeType) {
		Intent intent = new Intent();
		intent.putExtra(ConstantValue.NOTICE_TYPE, noticeType);
		// 后续需要实现的下拉时列表
		intent.setClass(this, NoticePullRefreshActivity.class);
		// intent.setClass(this, NoticeRecordActivity.class);
		startActivity(intent);
	}

	private void startToHomeworkActivity() {
		Intent intent = new Intent();
		intent.setClass(this, HomeworkPullRefreshActivity.class);
		startActivity(intent);
	}

	private void startToScheduleActivity() {
		Intent intent = new Intent();
		intent.setClass(this, ScheduleActivity.class);
		startActivity(intent);
	}

	private void startToCookbookActivity() {
		Intent intent = new Intent();
		intent.setClass(this, CookBookActivity.class);
		startActivity(intent);
	}

	private void startToSwipeCalendarActivity() {
		if (DataMgr.getInstance().getSelectedChild() == null) {
			Toast.makeText(this, R.string.child_info_is_null,
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent();
		intent.setClass(this, SwipeCalendarActivity.class);
		startActivity(intent);
	}

	private void startToSchoolInfoActivity() {
		Intent intent = new Intent();
		intent.setClass(this, SchoolInfoActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		unRegistObserver();
		MyThreadPoolMgr.shutdown();
		super.onDestroy();
	}

}
