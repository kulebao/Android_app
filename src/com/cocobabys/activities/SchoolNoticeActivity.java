package com.cocobabys.activities;

import io.rong.imkit.RongIM;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cocobabys.R;
import com.cocobabys.adapter.SchoolNoticeGridViewAdapter;
import com.cocobabys.bean.MainGridInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.constant.MemberStatus;
import com.cocobabys.customview.PointerPopupWindow;
import com.cocobabys.customview.RoundedImageView;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ChildInfo;
import com.cocobabys.dbmgr.info.IMGroupInfo;
import com.cocobabys.dbmgr.info.InfoHelper;
import com.cocobabys.dbmgr.info.NativeMediumInfo;
import com.cocobabys.dbmgr.info.Teacher;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.handler.TaskResultHandler;
import com.cocobabys.jobs.DownLoadImgAndSaveJob;
import com.cocobabys.jobs.GetClassRelationShipJob;
import com.cocobabys.jobs.GetTeacherListJob;
import com.cocobabys.jobs.JoinGroupJob;
import com.cocobabys.lbs.LbsMainActivity;
import com.cocobabys.media.MediaMgr;
import com.cocobabys.receiver.NotificationObserver;
import com.cocobabys.service.MyService;
import com.cocobabys.taskmgr.CheckChildrenInfoTask;
import com.cocobabys.taskmgr.DownLoadImgAndSaveTask;
import com.cocobabys.taskmgr.UploadInfoTask;
import com.cocobabys.threadpool.MyThreadPoolMgr;
import com.cocobabys.upload.UploadFactory;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.ImageUtils;
import com.cocobabys.utils.MethodUtils;
import com.cocobabys.utils.Utils;
import com.cocobabys.video.VideoApp;
import com.cocobabys.video.VideoLoginActivity;
import com.umeng.analytics.MobclickAgent;

import de.greenrobot.event.EventBus;

public class SchoolNoticeActivity extends TabChildActivity {
	private GridView gridview;
	public static final int NORMAL_NOTICE = 0;
	public static final int COOK_NOTICE = 1;
	public static final int SWAPCARD_NOTICE = 2;
	public static final int SCHEDULE = 3;
	// public static final int HOMEWORK = 4;
	public static final int INTERACTION = 4;
	public static final int EDUCATION = 5;
	public static final int EXPERENCE = 6;
	public static final int WATCH = 7;

	private static final long ONE_YEAR = 24 * 365 * 60 * 60 * 1000L;
	private static final int IMAGE_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	private static final int RESIZE_REQUEST_CODE = 2;
	private static final String IMAGE_FILE_NAME = "header.jpg";
	private RoundedImageView babyHeadIcon;
	private ChildInfo selectedChild;
	private AsyncTask<Void, Void, Integer> downloadIconTask;
	private Handler handler;
	private ProgressDialog progressDialog;
	private SchoolNoticeGridViewAdapter adapter;
	private NotificationObserver notificationObserver;

	private Map<Integer, Integer> map = new HashMap<Integer, Integer>() {
		private static final long serialVersionUID = 1L;

		{
			put(R.drawable.cook, R.string.want_cookbook);
			put(R.drawable.schedule, R.string.want_schedule);
			// put(HOMEWORK,
			// R.string.want_homework);
			put(R.drawable.chat, R.string.want_chat);
			put(R.drawable.education, R.string.want_estimation);
			put(R.drawable.exp, R.string.want_exp);
			put(R.drawable.bus_big, R.string.want_bus);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.school_notice);

		Log.d("", "ZZZZZ SchoolNoticeActivity oncreat");

		ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.app_name);

		initProgressDlg();
		initHandler();
		initUI();
		fixHeadIcon();
		// 检查小孩信息是否有更新，有更新需要及时更新
		runCheckChildrenInfoTask();

		registObserver();
		MobclickAgent.updateOnlineConfig(this);
		DataUtils.saveUndeleteableProp(ConstantValue.VERSION_CODE, DataUtils.getVersionCode() + "");
	}

	// 为了兼容之前的头像保存方式，这里做一个补丁处理
	private void fixHeadIcon() {
		ChildInfo childInfo = DataMgr.getInstance().getSelectedChild();
		if (childInfo != null && !TextUtils.isEmpty(childInfo.getServer_url())
				&& TextUtils.isEmpty(childInfo.getLocal_url())) {
			NativeMediumInfo info = new NativeMediumInfo();
			info.setKey(childInfo.getServer_url());
			info.setValue(childInfo.getLocal_url());
			DataMgr.getInstance().replace(info);
		}
	}

	private void runCheckClassRelationshipTask() {
		List<String> allClassID = DataMgr.getInstance().getAllClassID();
		Log.d("", "AAAA runCheckClassRelationshipTask class count =" + allClassID.size());
		for (String classid : allClassID) {
			GetClassRelationShipJob job = new GetClassRelationShipJob(handler, classid);
			job.execute();
		}
	}

	private void runCheckTeacherInfoTask() {
		try {
			List<Teacher> allTeachers = DataMgr.getInstance().getAllTeachers();
			// if(!allTeachers.isEmpty()){
			// new GetTeacherTask(Teacher.getPhones(allTeachers)).execute();
			// }

			ChildInfo child = DataMgr.getInstance().getSelectedChild();
			if (child != null) {
				Log.d("", "AAAA runCheckTeacherInfoTask  child=" + child.toString());
				GetTeacherListJob getTeacherListJob = new GetTeacherListJob(handler, child.getClass_id());
				getTeacherListJob.execute();
			}

			for (Teacher teacher : allTeachers) {
				// 如果教师的头像属性不为空，本地又没有保存，那么此时重新下载一次头像
				if (!TextUtils.isEmpty(teacher.getHead_icon()) && !new File(teacher.getLocalIconPath()).exists()) {
					Log.d("EEE", "download teacher icon name=" + teacher.getName() + " url=" + teacher.getHead_icon());
					new DownLoadImgAndSaveJob(teacher.getHead_icon(), teacher.getLocalIconPath(),
							ConstantValue.HEAD_ICON_WIDTH, ConstantValue.HEAD_ICON_HEIGHT).execute();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkNewDatas() {
		MethodUtils.executeCheckNewsCommand(this);
		MethodUtils.executeCheckHomeworkCommand(this);
		MethodUtils.executeCheckCookbookCommand(this);
		MethodUtils.executeCheckScheduleCommand(this);
		// MethodUtils.executeCheckChatCommand(this);
		MethodUtils.executeCheckEducationCommand(this);
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
		initGuardPic();

		initGridView();
		initBtn();
		initTitle();
		setTabTitle();
		setSchoolName();
		setBusinessEntry();
	}

	private void initGuardPic() {
		boolean firstStart = DataUtils.isFirstStart();
		// 新安装，版本升级，这里都返回true
		boolean upgrade = DataUtils.isVersionUpdate();

		if (firstStart) {
			setGuadPic(R.drawable.guardnew);
			DataUtils.setGuided();
		} else if (upgrade) {
			setGuadPic(R.drawable.guard_upgrade);
		}

	}

	private void setGuadPic(int picID) {
		final RelativeLayout guardlayout = (RelativeLayout) findViewById(R.id.guardlayout);
		final ImageView guardpic = (ImageView) findViewById(R.id.guardpic);
		guardpic.setImageResource(picID);
		guardlayout.setVisibility(View.VISIBLE);
		guardlayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				guardlayout.setVisibility(View.GONE);
			}
		});
	}

	private void initProgressDlg() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
	}

	private void runCheckChildrenInfoTask() {
		progressDialog.setMessage(getResources().getString(R.string.get_child_info));
		progressDialog.show();
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
				Log.w("dasd", "school notice msg.what=" + msg.what);
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.UPDATE_CHILDREN_INFO:
					setTabTitle();
					setClassName();
					break;
				case EventType.DOWNLOAD_FILE_SUCCESS:
					handleDownloadImgSuccess((String) msg.obj);
					break;
				case EventType.CHECK_NEW_DATA:
					checkNewDatas();
					// 小孩数据已经更新完毕，可以获取到班级信息，此时更新班级关系和教师信息了
					runCheckTeacherInfoTask();
					runCheckClassRelationshipTask();
					break;
				case EventType.SERVER_INNER_ERROR:
					Toast.makeText(SchoolNoticeActivity.this, R.string.get_child_info_fail, Toast.LENGTH_SHORT).show();
					break;
				case EventType.JOIN_IM_GROUP_SUCCESS:
					startToConversationListActivity();
					break;
				case EventType.JOIN_IM_GROUP_FAIL:
					handleJoinGroupFail();
					break;
				default:
					break;
				}
			}
		};
	}

	private void handleJoinGroupFail() {
		List<IMGroupInfo> allIMGroupInfo = DataMgr.getInstance().getAllIMGroupInfo();

		if (!allIMGroupInfo.isEmpty()) {
			startToConversationListActivity();
		} else {
			Utils.makeToast(this, R.string.join_group_failed);
		}
	}

	public void handleDownloadImgSuccess(String filepath) {
		DataMgr instance = DataMgr.getInstance();
		NativeMediumInfo info = new NativeMediumInfo();
		info.setKey(selectedChild.getServer_url());
		info.setValue(filepath);
		instance.replace(info);

		// instance.updateChildLocalIconUrl(instance.getSelectedChild().getServer_id(),
		// filepath);
		// // 本地文件保存更新,在之前如果为空的情况下，需要重新设置
		// selectedChild.setLocal_url(filepath);
		Bitmap loacalBitmap = Utils.getLoacalBitmap(filepath);
		Utils.setImg(babyHeadIcon, loacalBitmap);
	}

	private void setTabTitle() {
		selectedChild = DataMgr.getInstance().getSelectedChild();
		if (selectedChild != null) {
			setNickName();
			setBirthDay();
			setHeadIcon();
			setClassName();
		}
	}

	public void setClassName() {
		if (showBusiness()) {
			TextView classnameView = (TextView) findViewById(R.id.classname);
			classnameView.setVisibility(View.VISIBLE);
			ChildInfo child = DataMgr.getInstance().getSelectedChild();
			if (child != null) {
				classnameView.setText(child.getClass_name());
			}
		} else {
			TextView rightnameView = (TextView) findViewById(R.id.rightname);
			ChildInfo child = DataMgr.getInstance().getSelectedChild();
			if (child != null) {
				rightnameView.setText(child.getClass_name());
			}
		}
	}

	private void setBusinessEntry() {
		if (showBusiness()) {
			TextView rightnameView = (TextView) findViewById(R.id.rightname);
			rightnameView.setText(R.string.favourable);

			rightnameView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startToActivity(new ActivityLauncher() {
						@Override
						public void startActivity() {
							// startToShopLocActivity();
							// startToLbsActivity();
							startToBusinessActivity();
						}
					});
				}
			});
		}
	}

	private boolean showBusiness() {
		String value = DataUtils.getUndeleteableProp(ConstantValue.BUSINESS_STATE);
		return ConstantValue.BUSINESS_VISIBLE.equals(value);
	}

	private void startToBusinessActivity() {
		// 满足条件才显示在客户端
		if (showBusiness()) {
			Intent intent = new Intent();
			intent.setClass(this, BusinessActivity.class);
			startActivity(intent);
		}
	}

	private void startToShopLocActivity() {
		Intent intent = new Intent();
		intent.setClass(this, ShopLocationActivity.class);
		startActivity(intent);
	}

	private void startToSchoolbusActivity() {
		Intent intent = new Intent();
		intent.setClass(this, SchoolbusActivity.class);
		startActivity(intent);
	}

	private void setSchoolName() {
		TextView schoolSummaryView = (TextView) findViewById(R.id.schoolSummary);
		schoolSummaryView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startToSchoolInfoActivity();
			}
		});
	}

	private void setHeadIcon() {
		String url = selectedChild.getLocal_url();
		Log.d("", "setHeadIcon url=" + url);
		if (!"".equals(url)) {
			Bitmap loacalBitmap = Utils.getLoacalBitmap(url);
			if (loacalBitmap != null) {
				Utils.setImg(babyHeadIcon, loacalBitmap);
				return;
			}
		}

		babyHeadIcon.setImageResource(R.drawable.default_child_head_icon);
		if (!"".equals(selectedChild.getServer_url())) {
			if (downloadIconTask != null && downloadIconTask.getStatus() == AsyncTask.Status.RUNNING) {
				// 后执行的取消先执行的
				downloadIconTask.cancel(true);
			}

			Log.d("", "setHeadIcon begain download server url=" + url);
			downloadIconTask = new DownLoadImgAndSaveTask(handler, selectedChild.getServer_url(),
					InfoHelper.getChildrenLocalIconPath(selectedChild.getServer_id()), ConstantValue.BABY_HEAD_PIC_SIZE,
					ConstantValue.BABY_HEAD_PIC_SIZE).execute();
		}
	}

	private void setBirthDay() {
		Long birthDay = selectedChild.getChild_birthday();
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
			int month = current.get(Calendar.MONTH) - birthDay.get(Calendar.MONTH);

			if (month > 0) {
				age.append(String.format(getResources().getString(R.string.age_year), year));
				age.append(String.format(getResources().getString(R.string.age_month), month));
			} else if (month == 0) {
				age.append(String.format(getResources().getString(R.string.age_year), year));
			} else {
				year = year - 1;
				month = 12 + month;
				age.append(String.format(getResources().getString(R.string.age_year), year));
				age.append(String.format(getResources().getString(R.string.age_month), month));
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
			Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
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
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ConstantValue.START_SETTING && resultCode == ConstantValue.EXIT_LOGIN_RESULT) {
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
					Toast.makeText(SchoolNoticeActivity.this, "未找到存储卡，无法存储照片！", Toast.LENGTH_LONG).show();
				}
				break;

			case RESIZE_REQUEST_CODE:
				uploadIcon(data);
				break;
			}
		}

	}

	private void uploadIcon(Intent data) {
		if (data != null) {
			try {
				Bitmap bitmap = ImageUtils.getBitmap(data);
				JSONObject obj = InfoHelper.childInfoToJSONObject(selectedChild);
				obj.put(InfoHelper.PHOTO, UploadFactory.getUploadHost() + Utils.getUploadChildUrl());

				runUploadTask(obj.toString(), bitmap, getResources().getString(R.string.uploading_icon),
						new TaskResultHandler() {
							@Override
							public void handleResult(int result, Object param) {
								if (handleBefore(result)) {
									return;
								}
								if (result == EventType.UPLOAD_SUCCESS) {
									updateChildPhoto((Bitmap) param);
								} else {
									Toast.makeText(SchoolNoticeActivity.this, R.string.upload_icon_failed,
											Toast.LENGTH_SHORT).show();
								}
							}
						});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 此时是选中或拍好照片后，对照片进行裁剪
	public void resizeImage(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			String url = DataUtils.getPath(uri);
			intent.setDataAndType(Uri.fromFile(new File(url)), "image/*");
		} else {
			intent.setDataAndType(uri, "image/*");
		}

		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 200);
		intent.putExtra("outputY", 200);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, RESIZE_REQUEST_CODE);
	}

	private void runUploadTask(String content, Bitmap bitmap, String notice, TaskResultHandler task) {
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
			String path = InfoHelper.getChildrenLocalIconPath(selectedChild.getServer_id());
			Utils.saveBitmapToSDCard(photo, path);

			NativeMediumInfo info = new NativeMediumInfo();
			info.setKey(selectedChild.getServer_url());
			info.setValue(path);
			DataMgr.getInstance().replace(info);
			// DataMgr.getInstance().updateChildLocalIconUrl(selectedChild.getServer_id(),
			// path);
			// selectedChild.setLocal_url(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Uri getImageUri() {
		return Uri.fromFile(new File(Utils.getSDCardFileDir(Utils.APP_DIR_TMP), IMAGE_FILE_NAME));
	}

	private void setNickName() {
		TextView nicknamView = (TextView) findViewById(R.id.child_name);
		String name = selectedChild.getChild_nick_name();
		if (TextUtils.isEmpty(name)) {
			name = selectedChild.getChild_name();
		}
		nicknamView.setText(name);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d("", "ZZZZZ SchoolNoticeActivity onResume");
		handleResume();
	}

	private void handleResume() {
		try {
			adapter.notifyDataSetChanged();
			if (selectedChild != null && selectedChild.getId() != DataMgr.getInstance().getSelectedChild().getId()) {
				Log.d("DDD", "selectedChild changed redraw!");
				setTabTitle();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initTitle() {
		babyHeadIcon = (RoundedImageView) findViewById(R.id.child_photo);
		babyHeadIcon.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// showOptionDlg();
				showPopWindow();
			}
		});
	}

	private void showPopWindow() {
		// warning: you must specify the window width explicitly(do not use
		// WRAP_CONTENT or MATCH_PARENT)
		final PointerPopupWindow p = new PointerPopupWindow(this,
				getResources().getDimensionPixelSize(R.dimen.popup_width));
		View convertView = LayoutInflater.from(this).inflate(R.layout.child_option, null);
		View setNick = convertView.findViewById(R.id.setNick);
		View setBirthday = convertView.findViewById(R.id.setBirthday);
		View takepic = convertView.findViewById(R.id.takepic);
		View openGallery = convertView.findViewById(R.id.openGallery);

		setNick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				p.dismiss();
				showTextDlg();
			}
		});

		setBirthday.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				p.dismiss();
				showDateDlg();
			}
		});

		takepic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				p.dismiss();
				setBabyHeadIconByTakePhoto();
			}
		});

		openGallery.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				p.dismiss();
				setBabyHeadIconFromIconLib();
			}
		});

		p.setContentView(convertView);
		p.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.pop_window_back)));
		p.setPointerImageRes(R.drawable.ic_popup_pointer);
		p.setAlignMode(PointerPopupWindow.AlignMode.CENTER_FIX);
		p.showAsPointer(babyHeadIcon);
	}

	public void uploadBirthday(Calendar birthday) {
		final long born = birthday.getTimeInMillis();

		try {
			JSONObject obj = InfoHelper.childInfoToJSONObject(selectedChild);
			obj.put(InfoHelper.BIRTHDAY, InfoHelper.getYearMonthDayFormat().format(new Date(born)));

			runUploadTask(obj.toString(), null, getResources().getString(R.string.uploading_child_info),
					new TaskResultHandler() {
						@Override
						public void handleResult(int result, Object param) {
							if (handleBefore(result)) {
								return;
							}
							if (result == EventType.UPLOAD_SUCCESS) {
								DataMgr.getInstance().updateBirthday(selectedChild.getServer_id(), born);
								selectedChild.setChild_birthday(born);
								setBirthDay();
							} else {
								Toast.makeText(SchoolNoticeActivity.this, R.string.uploading_child_info_failed,
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
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				Calendar birthday = Calendar.getInstance();
				birthday.set(year, monthOfYear, dayOfMonth);
				Log.d("", "date :" + birthday.toString());

				if (!checkValid(birthday)) {
					Toast.makeText(SchoolNoticeActivity.this, R.string.baby_age_limit, Toast.LENGTH_SHORT).show();
					return;
				}

				uploadBirthday(birthday);
			}

		};

		// 把日期控件的初始时间设置为小孩当前生日
		Long birthDay = selectedChild.getChild_birthday();
		calendar.setTimeInMillis(birthDay);

		final MyDatePickerDialog dialog = new MyDatePickerDialog(this, dateListener, calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

		DialogInterface.OnClickListener listener = null;

		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, Utils.getResString(R.string.cancel), listener);

		dialog.setButton(DialogInterface.BUTTON_POSITIVE, Utils.getResString(R.string.confirm), dialog);

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
		final View textEntryView = LayoutInflater.from(this).inflate(R.layout.text_entry, null);
		final EditText nicknameEdit = (EditText) textEntryView.findViewById(R.id.content);
		new AlertDialog.Builder(this).setTitle(R.string.set_baby_nickname).setView(textEntryView)
				.setPositiveButton(R.string.confirm, new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final String nick = nicknameEdit.getText().toString().replace(" ", "");

						if (TextUtils.isEmpty(nick)) {
							Toast.makeText(SchoolNoticeActivity.this, getResources().getString(R.string.invalid_nick),
									Toast.LENGTH_SHORT).show();
							return;
						}
						uploadNick(nick);
					}

				}).setNegativeButton(R.string.back, null).create().show();
	}

	public void uploadNick(final String nick) {
		try {
			JSONObject obj = InfoHelper.childInfoToJSONObject(selectedChild);
			obj.put(InfoHelper.NICK, nick);

			runUploadTask(obj.toString(), null, getResources().getString(R.string.uploading_child_info),
					new TaskResultHandler() {
						@Override
						public void handleResult(int result, Object param) {
							if (handleBefore(result)) {
								return;
							}

							if (result == EventType.UPLOAD_SUCCESS) {
								DataMgr.getInstance().updateNick(selectedChild.getServer_id(), nick);
								selectedChild.setChild_nick_name(nick);
								setNickName();
							} else {
								Toast.makeText(SchoolNoticeActivity.this, R.string.uploading_child_info_failed,
										Toast.LENGTH_SHORT).show();
							}
						}

					});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private boolean handleBefore(int result) {
		progressDialog.cancel();

		if (result == EventType.PHONE_NUM_IS_ALREADY_LOGIN) {
			Toast.makeText(this, R.string.phone_num_is_already_login, Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}

	private void initBtn() {
		TextView settingBtn = (TextView) findViewById(R.id.settingBtn);
		settingBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startToSettingActivity();
			}
		});
	}

	public void initGridView() {
		gridview = (GridView) findViewById(R.id.gridview);
		ArrayList<MainGridInfo> lstImageItem = initData();
		adapter = new SchoolNoticeGridViewAdapter(this, lstImageItem);
		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(new ItemClickListener());
	}

	public ArrayList<MainGridInfo> initData() {
		ArrayList<MainGridInfo> lstImageItem = new ArrayList<MainGridInfo>();

		MainGridInfo info = new MainGridInfo();
		info.setResID(R.drawable.pnotice);
		info.setTitleID(R.string.noticeTitle);
		lstImageItem.add(info);

		info = new MainGridInfo();
		info.setResID(R.drawable.schedule);
		info.setTitleID(R.string.schedule);
		lstImageItem.add(info);

		info = new MainGridInfo();
		info.setResID(R.drawable.cook);
		info.setTitleID(R.string.cook);
		lstImageItem.add(info);

		info = new MainGridInfo();
		info.setResID(R.drawable.chat);
		info.setTitleID(R.string.interaction);
		lstImageItem.add(info);

		info = new MainGridInfo();
		info.setResID(R.drawable.exp);
		info.setTitleID(R.string.experence);
		lstImageItem.add(info);

		info = new MainGridInfo();
		info.setResID(R.drawable.swap);
		info.setTitleID(R.string.swap);
		lstImageItem.add(info);

		info = new MainGridInfo();
		info.setResID(R.drawable.education);
		info.setTitleID(R.string.education);
		lstImageItem.add(info);

		info = new MainGridInfo();
		info.setResID(R.drawable.bus_big);
		info.setTitleID(R.string.bus_service);
		lstImageItem.add(info);

		// 如果幼儿园属性里面隐藏了视频模块，则不显示
		if ("false".equals(DataUtils.getProp(JSONConstant.HIDE_VIDEO, "false"))) {
			info = new MainGridInfo();
			info.setResID(R.drawable.watch);
			info.setTitleID(R.string.watch_baby);
			lstImageItem.add(info);
		}

		return lstImageItem;
	}

	// 当AdapterView被单击(触摸屏或者键盘)，则返回的Item单击事件
	private class ItemClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			handleGridViewClick(position);
		}
	}

	public void handleGridViewClick(int position) {
		// 未获取到小孩信息时，不能响应按键
		if (selectedChild == null) {
			Utils.makeToast(this, R.string.reget_child_info);
			return;
		}

		MainGridInfo item = adapter.getItem(position);
		int resID = item.getResID();

		String member_status = MemberStatus.FREE.toString();

		if (member_status.equalsIgnoreCase(DataUtils.getProp(JSONConstant.MEMBER_STATUS, member_status))) {
			// SWAPCARD_NOTICE 和 NORMAL_NOTICE 免费开放 ，WATCH是单独的权限控制
			if (R.drawable.swap != resID && R.drawable.pnotice != resID && R.drawable.watch != resID) {
				Utils.makeToast(this, map.get(resID));
				return;
			}
		}

		handleIconClick(resID);
	}

	private void handleIconClick(int resID) {
		switch (resID) {
		case R.drawable.bus_big:
			startToActivity(new ActivityLauncher() {
				@Override
				public void startActivity() {
					startToSchoolbusActivity();
				}
			});
			break;
		case R.drawable.cook:
			startToActivity(new ActivityLauncher() {
				@Override
				public void startActivity() {
					startToCookbookActivity();
				}
			});
			break;
		case R.drawable.swap:
			startToActivity(new ActivityLauncher() {
				@Override
				public void startActivity() {
					startToSwipeCalendarActivity();
				}
			});
			break;
		case R.drawable.pnotice:
			startToActivity(new ActivityLauncher() {
				@Override
				public void startActivity() {
					startToNoticeListActivity();
				}
			});
			break;
		case R.drawable.schedule:
			startToActivity(new ActivityLauncher() {
				@Override
				public void startActivity() {
					startToScheduleActivity();
				}
			});
			break;
		case R.drawable.chat:
			startToActivity(new ActivityLauncher() {
				@Override
				public void startActivity() {
					handleConversationBtnClick();
				}
			});
			break;
		case R.drawable.education:
			startToActivity(new ActivityLauncher() {
				@Override
				public void startActivity() {
					startToEducationActivity();
				}
			});
			break;
		case R.drawable.exp:
			startToActivity(new ActivityLauncher() {
				@Override
				public void startActivity() {
					startToGrowthActivity();
				}
			});
			break;
		case R.drawable.watch:
			startToActivity(new ActivityLauncher() {
				@Override
				public void startActivity() {
					startToLoginVideo();
				}
			});
			break;
		default:
			Utils.makeToast(this, "暂未实现！");
			break;
		}
	}

	private void startToConversationListActivity() {
		try {
			// RongIM.getInstance().startPrivateChat(SchoolNoticeActivity.this,
			// "t_8901_Some(1513)_20414",
			// "郭晓明");
			RongIM.getInstance().startConversationList(SchoolNoticeActivity.this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startToLoginVideo() {
		if (!Utils.isNetworkConnected(this)) {
			Utils.makeToast(this, R.string.net_error);
			return;
		}
		Utils.goNextActivity(this, VideoLoginActivity.class, false);
	}

	protected void startToGrowthActivity() {
		Intent intent = new Intent();
		intent.setClass(this, ExpActivity.class);
		startActivity(intent);
	}

	private void startToActivity(ActivityLauncher launcher) {
		ActivityLauncherProxy proxy = new ActivityLauncherProxy();
		ActivityLauncher launcherimpl = (ActivityLauncher) proxy.bind(launcher);
		launcherimpl.startActivity();
	}

	private void startToEducationActivity() {
		Intent intent = new Intent();
		intent.setClass(this, EducationActivity.class);
		startActivity(intent);
	}

	private void startToChatActivity() {
		Intent intent = new Intent();
		intent.setClass(this, ChatActivity.class);
		startActivity(intent);
	}

	private void startToSettingActivity() {
		if (selectedChild == null) {
			Utils.makeToast(this, R.string.reget_child_info);
			return;
		}
		Intent intent = new Intent();
		intent.setClass(this, SettingActivity.class);
		startActivityForResult(intent, ConstantValue.START_SETTING);
	}

	private void startToNoticeListActivity() {
		Intent intent = new Intent();
		intent.setClass(this, NoticePullRefreshActivity.class);
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
			Toast.makeText(this, R.string.child_info_is_null, Toast.LENGTH_SHORT).show();
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
		handleAppExit();
		super.onDestroy();
	}

	private void handleAppExit() {
		unRegistObserver();
		MyThreadPoolMgr.shutdown();
		DataMgr.getInstance().close();
		MediaMgr.close();
		// VideoApp.getJni().uninit();
		VideoApp.close();
		EventBus.clearCaches();
		ImageUtils.clearCache();

		Intent service = new Intent(this, MyService.class);
		stopService(service);
	}

	private void startToLbsActivity() {
		Intent intent = new Intent(SchoolNoticeActivity.this, LbsMainActivity.class);
		startActivity(intent);
	}

	private void handleConversationBtnClick() {
		// if(MyApplication.getInstance().isForTest()){
		List<String> allClassID = DataMgr.getInstance().getAllClassID();
		List<String> needJoinClassID = new ArrayList<>();

		for (String class_id : allClassID) {
			IMGroupInfo imGroupInfo = DataMgr.getInstance().getIMGroupInfo(Integer.parseInt(class_id));
			if (imGroupInfo == null) {
				needJoinClassID.add(class_id);
			}
		}

		if (!needJoinClassID.isEmpty()) {
			runJoinGroupTask(handler, needJoinClassID);
		} else {
			startToConversationListActivity();
		}
		// } else{
		// startToChatActivity();
		// }
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d("", "SchoolNoticeActivity onNewIntent");
	}

	protected void runJoinGroupTask(Handler handler, List<String> classidList) {
		progressDialog.setMessage(getResources().getString(R.string.get_group_info));
		progressDialog.show();
		JoinGroupJob joinGroupJob = new JoinGroupJob(handler, classidList);
		joinGroupJob.execute();
	}

	private interface ActivityLauncher {
		public void startActivity();
	}

	private class MyDatePickerDialog extends DatePickerDialog {

		public MyDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear,
				int dayOfMonth) {
			super(context, callBack, year, monthOfYear, dayOfMonth);
		}

		@Override
		protected void onStop() {
			// 注释掉，否则点back键时会触发onDateSet方法
			// super.onStop();
		}
	}

	private class ActivityLauncherProxy implements InvocationHandler {
		private Object target;

		/**
		 * 绑定委托对象并返回一个代理类
		 * 
		 * @param target
		 * @return
		 */
		public Object bind(Object target) {
			this.target = target;
			// 取得代理对象
			return Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this); // 要绑定接口(这是一个缺陷，cglib弥补了这一缺陷)
		}

		@Override
		/**
		 * 调用方法
		 */
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Object result = null;
			Log.d("DDD", "check child info!!!!");
			if (DataMgr.getInstance().getSelectedChild() == null) {
				Toast.makeText(SchoolNoticeActivity.this, R.string.child_info_is_null, Toast.LENGTH_SHORT).show();
				return null;
			}
			// 执行方法
			result = method.invoke(target, args);
			return result;
		}

	}
}
