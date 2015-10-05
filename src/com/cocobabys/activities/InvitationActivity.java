package com.cocobabys.activities;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.InfoHelper;
import com.cocobabys.dbmgr.info.SchoolInfo;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.InvitationJob;
import com.cocobabys.taskmgr.DownLoadImgAndSaveTask;
import com.cocobabys.taskmgr.GetAuthCodeTask;
import com.cocobabys.taskmgr.GetSchoolInfoTask;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

public class InvitationActivity extends UmengStatisticsActivity {

	private ImageView logo;
	private TextView desc;
	private Button contact_school;
	private MyHandler handler;
	private ProgressDialog dialog;
	private SchoolInfo info = new SchoolInfo();
	private AsyncTask<Void, Void, Integer> downloadIconTask;
	private EditText vCode;

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
				if (InvitationActivity.this.isFinishing()) {
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
				case EventType.INVITE_FAIL:
					Utils.makeToast(InvitationActivity.this, "邀请失败");
					break;
				case EventType.INVITE_SUCCESS:
					Utils.makeToast(InvitationActivity.this, "邀请成功");
					break;
				case EventType.GET_AUTH_CODE_FAIL:
					Utils.makeToast(InvitationActivity.this, "获取验证码失败");
					break;
				case EventType.GET_AUTH_CODE_SUCCESS:
					Utils.makeToast(InvitationActivity.this, "获取验证码成功");
					break;
				case EventType.GET_AUTH_CODE_TOO_OFTEN:
					Utils.makeToast(InvitationActivity.this, R.string.get_auth_code_too_often);
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

	public void getcode(View view) {
		new GetAuthCodeTask(handler, DataUtils.getAccount(), ConstantValue.TYPE_GET_REG_AUTHCODE).execute();
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
		vCode = (EditText) findViewById(R.id.vCode);
		logo = (ImageView) findViewById(R.id.school_logo);
		desc = (TextView) findViewById(R.id.school_desc);
		contact_school = (Button) findViewById(R.id.contact_school);
	}

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
					if (MyApplication.getInstance().isForTest()) {
						invitation();
						return;
					}

					if (!"".equals(info.getSchool_phone())) {
						Utils.startToCall(InvitationActivity.this, info.getSchool_phone());
					}
				}
			});
		} else {
			contact_school.setVisibility(View.GONE);
		}
	}

	protected void invitation() {
		InvitationJob invitationJob = new InvitationJob(handler, "44443333222", "袋鼠测试A", "爷爷", getEditeContent());
		invitationJob.execute();
	}

	private String getEditeContent() {
		return vCode.getText().toString() == null ? "" : vCode.getText().toString();
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
