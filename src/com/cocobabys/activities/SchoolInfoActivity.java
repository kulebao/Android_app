package com.cocobabys.activities;

import com.cocobabys.R;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.InfoHelper;
import com.cocobabys.dbmgr.info.SchoolInfo;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.taskmgr.DownLoadImgAndSaveTask;
import com.cocobabys.taskmgr.GetSchoolInfoTask;
import com.cocobabys.utils.Utils;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SchoolInfoActivity extends UmengStatisticsActivity {

	private ImageView logo;
	private TextView desc;
	private Button contact_school;
	private MyHandler handler;
	private ProgressDialog dialog;
	private SchoolInfo info = new SchoolInfo();
	private AsyncTask<Void, Void, Integer> downloadIconTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.school_info);
		ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.school_info);
		initDlg();
		initHandler();
		initUI();
		boolean firstQuery = showSchoolInfo();
		if (firstQuery) {
			dialog.show();
		}

		// 每次进来都从服务器查询是否有更新，有则刷新
		runGetSchoolInfoTask();
	}

	private void initDlg() {
		dialog = new ProgressDialog(this);
		dialog.setMessage(getResources().getString(R.string.get_school_info));
	}

	private void runGetSchoolInfoTask() {
		new GetSchoolInfoTask(handler).execute();
	}

	private void initHandler() {
		handler = new MyHandler(this, dialog) {
			@Override
			public void handleMessage(Message msg) {
				if (SchoolInfoActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.UPDATE_SCHOOL_INFO:
					// 数据已经保存于数据库
					showSchoolInfo();
					break;
				case EventType.SCHOOL_INFO_IS_LATEST:
					// do nothing
					break;
				case EventType.DOWNLOAD_FILE_SUCCESS:
					Log.d("DDD", "DOWNLOAD_IMG_SUCCESS");
					handleDownloadSchoolLogoSuccess((String) msg.obj);
					break;
				default:
					break;
				}
			}
		};
	}

	public void handleDownloadSchoolLogoSuccess(String filepath) {
		DataMgr instance = DataMgr.getInstance();
		instance.updateSchoolLogoLocalUrl(instance.getSchoolID(), filepath);
		// 本地文件保存更新,在之前如果为空的情况下，需要重新设置
		info.setSchool_logo_local_url(filepath);
		Bitmap loacalBitmap = Utils.getLoacalBitmap(filepath);
		Log.d("DDD", "handleDownloadSchoolLogoSuccess filepath=" + filepath);
		logo.setVisibility(View.VISIBLE);
		Utils.setImg(logo, loacalBitmap);
	}

	private void initUI() {
		logo = (ImageView) findViewById(R.id.school_logo);
		desc = (TextView) findViewById(R.id.school_desc);
		contact_school = (Button) findViewById(R.id.contact_school);
	}

	// private void showPushInfo(){
	// if(MyApplication.getInstance().isForTest()){
	// String USER_ID = DataUtils.getUndeleteableProp(JSONConstant.USER_ID);
	// String CHANNEL_ID =
	// DataUtils.getUndeleteableProp(JSONConstant.CHANNEL_ID);
	//
	// String text = "\nUSER_ID =" + USER_ID + "" + "\n" + "CHANNEL_ID =" +
	// CHANNEL_ID + "\n";
	// desc.setText(text);
	// }
	// }

	// 返回是否首次查询，首次查询需要显示loading动画
	private boolean showSchoolInfo() {
		info = DataMgr.getInstance().getSchoolInfo();
		if (!"".equals(info.getSchool_desc())) {
			showImpl();
			return false;
		}

		return true;
	}

	private void showImpl() {
		showLogo();
		showDesc();
		showContact();
	}

	private void showContact() {
		final String phonenum = info.getSchool_phone();
		if (!"".equals(phonenum)) {
			contact_school.setVisibility(View.VISIBLE);
			contact_school.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!"".equals(info.getSchool_phone())) {
						Utils.startToCall(SchoolInfoActivity.this, info.getSchool_phone());
					}
				}
			});
		} else {
			contact_school.setVisibility(View.GONE);
		}
	}

	private void showDesc() {
		String school_desc = info.getSchool_desc();
		desc.setText(school_desc);
	}

	private void showLogo() {
		String url = info.getSchool_logo_local_url();
		Log.d("DDD", "showLogo local  url=" + url);
		Log.d("DDD", "showLogo server url=" + info.getSchool_logo_server_url());
		if (!"".equals(url)) {
			logo.setVisibility(View.VISIBLE);
			Bitmap loacalBitmap = Utils.getLoacalBitmap(url);
			Utils.setImg(logo, loacalBitmap);
		} else if (!"".equals(info.getSchool_logo_server_url())) {

			if (downloadIconTask != null && downloadIconTask.getStatus() == AsyncTask.Status.RUNNING) {
				// 有下载任务正在执行
				Log.d("DDD", " downloading school icon");
				return;
			}

			downloadIconTask = new DownLoadImgAndSaveTask(handler, info.getSchool_logo_server_url(),
					InfoHelper.getDefaultSchoolLocalIconPath()).execute();
		}
	}

}
