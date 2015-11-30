package com.cocobabys.activities;

import java.io.File;
import java.util.ArrayList;

import com.cocobabys.R;
import com.cocobabys.adapter.SettingListAdapter;
import com.cocobabys.bean.SettingInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.customview.PointerPopupWindow;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.UpdateParentJob;
import com.cocobabys.taskmgr.CheckUpdateTask;
import com.cocobabys.upload.UploadFactory;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.ImageUtils;
import com.cocobabys.utils.Utils;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

public class SettingActivity extends UmengStatisticsActivity {
	private Handler handler;
	private ProgressDialog dialog;
	private ArrayList<SettingInfo> firstListinfo;
	private SettingListAdapter adapter;
	private ListView firstlist;
	private ImageView photo;

	private static final int IMAGE_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	private static final int RESIZE_REQUEST_CODE = 2;
	private static final String IMAGE_FILE_NAME = "parent_header.jpg";
	private static final int CHANGE_CHILD = 5;
	private String newPortrait;
	private Bitmap loacalBitmap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.setting);
		initHead();
		initList();
		initDialog();
		initHandler();

		setTestBtn();
	}

	private void showPopWindow() {
		// warning: you must specify the window width explicitly(do not use
		// WRAP_CONTENT or MATCH_PARENT)
		final PointerPopupWindow p = new PointerPopupWindow(this,
				getResources().getDimensionPixelSize(R.dimen.popup_width));
		View convertView = LayoutInflater.from(this).inflate(R.layout.parent_option, null);
		View takepic = convertView.findViewById(R.id.takepic);
		View openGallery = convertView.findViewById(R.id.openGallery);

		takepic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				p.dismiss();
				takePhoto();
			}
		});

		openGallery.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				p.dismiss();
				chooseFromGallery();
			}
		});

		p.setContentView(convertView);
		p.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));
		p.setPointerImageRes(R.drawable.ic_popup_pointer);
		p.setAlignMode(PointerPopupWindow.AlignMode.CENTER_FIX);
		p.showAsPointer(photo);
	}

	private void chooseFromGallery() {
		Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
		galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
		galleryIntent.setType("image/*");
		startActivityForResult(galleryIntent, IMAGE_REQUEST_CODE);
	}

	private void takePhoto() {
		if (Utils.isSdcardExisting()) {
			Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, getImageUri());
			cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
			startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
		} else {
			Toast.makeText(this, "请插入sd卡", Toast.LENGTH_SHORT).show();
		}
	}

	private Uri getImageUri() {
		return Uri.fromFile(new File(Utils.getSDCardFileDir(Utils.APP_DIR_TMP), IMAGE_FILE_NAME));
	}

	private void initHead() {
		photo = (ImageView) findViewById(R.id.photo);
		ParentInfo parentInfo = DataMgr.getInstance().getSelfInfoByPhone();
		TextView relation = (TextView) findViewById(R.id.relation);
		relation.setText(parentInfo.getFixedRelationShip(DataMgr.getInstance().getSelectedChild().getServer_id()));

		TextView name = (TextView) findViewById(R.id.name);
		name.setText(parentInfo.getName());

		TextView phone = (TextView) findViewById(R.id.phone);
		phone.setText(parentInfo.getPhone());
		String portrait = parentInfo.getPortrait();

		showPhoto(portrait);

		photo.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// showOptionDlg();
				showPopWindow();
			}
		});
	}

	private void showPhoto(String portrait) {
		if (!TextUtils.isEmpty(portrait)) {
			// 显示头像应该分为2步，
			// 1 如果本地IMAGE_FILE_NAME存在，则加载时显示
			// 2 不管IMAGE_FILE_NAME是否存在，都根据家长头像地址从服务器下载，以免家长在web端更新后，客户端不知道
			// ImageUtils.getImageLoader().displayImage(portrait, photo, new
			// SimpleImageLoadingListener() {
			// @Override
			// public void onLoadingStarted(String imageUri, View view) {
			// super.onLoadingStarted(imageUri, view);
			// showLocalBmp();
			// }
			// });
			ImageUtils.getImageLoader().displayImage(portrait, photo);
		}
	}

	private void showLocalBmp() {
		String path = new File(Utils.getSDCardFileDir(Utils.APP_DIR_TMP), IMAGE_FILE_NAME).getPath();
		Log.d("", "initHead path=" + path);
		loacalBitmap = Utils.getLoacalBitmap(path, 200, 200);
		if (loacalBitmap != null) {
			photo.setImageBitmap(loacalBitmap);
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

	@Override
	// 处理从图库和拍照返回的照片
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK) {
			// Toast.makeText(this, "头像选择失败，错误码:" + resultCode,
			// Toast.LENGTH_SHORT).show();
			return;
		}

		switch (requestCode) {
		case IMAGE_REQUEST_CODE:
			resizeImage(data.getData());
			break;
		case CAMERA_REQUEST_CODE:
			if (Utils.isSdcardExisting()) {
				resizeImage(getImageUri());
			} else {
				Toast.makeText(this, "未找到存储卡，无法存储照片！", Toast.LENGTH_LONG).show();
			}
			break;

		case RESIZE_REQUEST_CODE:
			uploadIcon(data);
			break;

		case CHANGE_CHILD:
			initHead();
			break;
		}
	}

	private void uploadIcon(Intent data) {
		if (data != null) {
			try {
				Bitmap bitmap = ImageUtils.getBitmap(data);
				String relativePath = Utils.getUploadParentUrl(System.currentTimeMillis());
				newPortrait = UploadFactory.getUploadHost() + relativePath;
				Log.d("", "uploadIcon portrait=" + newPortrait + "\n relativePath=" + relativePath);
				// do upload here
				dialog.setMessage(getResources().getString(R.string.uploading_icon));
				dialog.show();

				UpdateParentJob job = new UpdateParentJob(handler, newPortrait, bitmap, relativePath);
				job.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void initList() {
		initData();
		adapter = new SettingListAdapter(this, firstListinfo);
		firstlist = (ListView) findViewById(R.id.firstlist);
		firstlist.setAdapter(adapter);

		firstlist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SettingInfo item = adapter.getItem(position);
				handle(item);
			}
		});
	}

	protected void handle(SettingInfo item) {
		switch (item.getNameid()) {
		case R.string.updateCard:
			updateCard();
			break;
		case R.string.invitation:
			invite();
			break;
		case R.string.check_version:
			runCheckUpdateTask();
			break;
		case R.string.user_response:
			startToFeedBackActivity();
			break;
		case R.string.change_pwd:
			startToChangePWDActivity();
			break;
		case R.string.change_child:
			startToSelectChildActivity();
			break;
		case R.string.about_cocobabys:
			startToAboutUsActivity();
			break;
		case R.string.exit_login:
			Utils.showTwoBtnResDlg(R.string.confirm_exit, this, new android.content.DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					handleExitLogin();
				}

			});
			break;

		default:
			break;
		}
	}

	private void initData() {
		firstListinfo = new ArrayList<SettingInfo>() {
			private static final long serialVersionUID = 1L;

			{
				add(new SettingInfo(R.string.invitation, R.drawable.family_logo));
				add(new SettingInfo(R.string.updateCard, R.drawable.card_logo));
				add(new SettingInfo(R.string.check_version, R.drawable.update_logo));
				add(new SettingInfo(R.string.user_response, R.drawable.feedback_logo));
				add(new SettingInfo(R.string.change_pwd, R.drawable.password_logo));
				add(new SettingInfo(R.string.change_child, R.drawable.change_logo));
				add(new SettingInfo(R.string.about_cocobabys, R.drawable.about_logo));
				add(new SettingInfo(R.string.exit_login, R.drawable.quit_logo));
			}
		};
	}

	private void setTestBtn() {
		if (MyApplication.getInstance().isForTest()) {
			setHostBtn();
			setVideoBtn();
		}
	}

	private void setVideoBtn() {
		final Button changeVideo = (Button) findViewById(R.id.changeVideo);
		changeVideo.setVisibility(View.VISIBLE);
		setVideoBtnText(changeVideo);

		changeVideo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean isMyVideo = Utils.isMyVideo();
				if (isMyVideo) {
					Utils.setVideo("false");
				} else {
					Utils.setVideo("true");
				}
				setVideoBtnText(changeVideo);
			}
		});
	}

	private void setVideoBtnText(final Button changeVideo) {
		if (Utils.isMyVideo()) {
			changeVideo.setText("切换到其他摄像头");
		} else {
			changeVideo.setText("切换到自己的摄像头");
		}
	}

	private void setHostBtn() {
		final Button changeHost = (Button) findViewById(R.id.changeHost);
		changeHost.setVisibility(View.VISIBLE);
		setHostText(changeHost);
		changeHost.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean isTestHost = Utils.isTestHost();
				if (isTestHost) {
					Utils.setToTestHost("false");
				} else {
					Utils.setToTestHost("true");
				}

				DataMgr.getInstance().upgradeAll();
				Utils.clearSDFolder();
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.exit(0);
					}
				}).start();
			}
		});
	}

	private void setHostText(Button hostBtn) {
		if (Utils.isTestHost()) {
			hostBtn.setText("切换到商用地址");
		} else {
			hostBtn.setText("切换到测试地址");
		}
	}

	protected void startToAboutUsActivity() {
		Intent intent = new Intent();
		intent.setClass(this, AboutUsActivity.class);
		startActivity(intent);
	}

	protected void startToFeedBackActivity() {
		if (MyApplication.getInstance().isForTest()) {
			RongIM.getInstance().startConversation(this, Conversation.ConversationType.APP_PUBLIC_SERVICE,
					"KEFU144879042344018", "客服");
		} else {
			Intent intent = new Intent();
			intent.setClass(this, FeedBackActivity.class);
			startActivity(intent);
		}
	}

	private void initDialog() {
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
	}

	private void initHandler() {

		handler = new MyHandler(this, dialog) {

			@Override
			public void handleMessage(Message msg) {
				if (SettingActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.HAS_NEW_VERSION:
					startToUpdateActivity();
					break;
				case EventType.HAS_NO_VERSION:
					Utils.makeToast(SettingActivity.this, R.string.no_new_version);
					break;
				case EventType.UPDATE_PARENT_FAIL:
					Utils.makeToast(SettingActivity.this, R.string.upload_icon_failed);
					break;
				case EventType.UPDATE_PARENT_SUCCESS:
					handleUpdateSuccess((Bitmap) msg.obj);
					break;
				default:
					break;
				}
			}
		};
	}

	protected void handleUpdateSuccess(Bitmap bitmap) {
		releaseBmp();
		photo.setImageBitmap(bitmap);
		loacalBitmap = bitmap;

		// 此时下载服务器上图片，保存到缓存
		ParentInfo parentInfo = DataMgr.getInstance().getSelfInfoByPhone();
		ImageUtils.getImageLoader().loadImage(parentInfo.getPortrait(), new SimpleImageLoadingListener());
	}

	protected void runCheckUpdateTask() {
		// 如果后台在自动更新，这里可能会有冲突，后续需要把后台任务统一管理起来
		dialog.setMessage(getResources().getString(R.string.check_version));
		dialog.show();
		DataUtils.saveCheckNewTime(System.currentTimeMillis());
		new CheckUpdateTask(handler, DataUtils.getAccount(), DataUtils.getVersionCode()).execute();
	}

	private void handleExitLogin() {
		DataUtils.clearProp();
		DataMgr.getInstance().upgradeAll();
		Utils.clearSDFolder();
		setResult(ConstantValue.EXIT_LOGIN_RESULT);
		finish();
	}

	private void startToChangePWDActivity() {
		Intent intent = new Intent();
		intent.setClass(this, ChangePWDActivity.class);
		startActivity(intent);
	}

	private void startToUpdateActivity() {
		Intent intent = new Intent();
		intent.setClass(this, UpdateActivity.class);
		startActivity(intent);
	}

	private void startToSelectChildActivity() {
		Intent intent = new Intent();
		intent.setClass(this, ChildListActivity.class);
		startActivityForResult(intent, CHANGE_CHILD);
	}

	public void updateCard() {
		Intent intent = new Intent();
		intent.setClass(this, CardManagerActivity.class);
		startActivity(intent);
	}

	public void invite() {
		Intent intent = new Intent();
		intent.setClass(this, RelationListActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseBmp();
	}

	private void releaseBmp() {
		if (loacalBitmap != null) {
			loacalBitmap.recycle();
		}
	}

}
