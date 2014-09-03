package com.cocobabys.video;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cocobabys.R;
import com.cocobabys.activities.ActivityHelper;
import com.cocobabys.activities.UmengStatisticsActivity;
import com.cocobabys.constant.EventType;
import com.cocobabys.customview.CustomDialog;
import com.cocobabys.dlgmgr.DlgMgr;
import com.cocobabys.jobs.LoginVideoJob;
import com.cocobabys.utils.Utils;

/**
 * 登录平台，获取设备列表
 * 
 * @author admin
 * 
 */
public class VideoLoginActivity extends UmengStatisticsActivity {
	private static final String TAG = "LoginDemo";

	private ProgressDialog loginProcessDialog;
	private Handler handler;
	private String username = "2222";// "2222";// "xmm";// "cocbaby";
	private String password = "6yWw2D";// "6yWw2D";// "123456";// "13880498549";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_login);
		ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.watch_baby);

		VideoApp.getJni().init();
		registerHander();
		runLogin();
	}

	public void runLogin() {
		showWaitDialog("logining...");
		Log.d(TAG, "username: " + username + ", password: " + password);
		LoginVideoJob job = new LoginVideoJob(handler, username, password);
		job.execute();
	}

	private void showWaitDialog(String str) {
		loginProcessDialog = ProgressDialog.show(this, null, str);
		loginProcessDialog.setCancelable(false);
	}

	private void registerHander() {
		handler = new MyHandler() {
			@Override
			public void handleMessage(Message msg) {
				if (VideoLoginActivity.this.isFinishing()) {
					return;
				}
				closeWaitDoalog();

				if (msg.what == EventType.VIDEO_LOGIN_SUCCESS) {
					gotoDeviceListActivity();
				} else if (msg.what == EventType.VIDEO_GET_INFO_NOT_REG) {
					CustomDialog.Builder builder = DlgMgr.getSingleBtnDlg(
							VideoLoginActivity.this, new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									VideoLoginActivity.this.finish();
								}
							});
					builder.setMessage("还未开通\"看宝贝\"功能,该功能可以让家长通过视频，实时查看孩子在幼儿园的动态,如有需要"
							+ "请联系幼儿园开通");
					builder.create().show();
				} else {
					VideoApp.getJni().disconnectServer(VideoApp.serverId);
					Utils.makeToast(VideoLoginActivity.this, "登录视频服务器失败");
					VideoLoginActivity.this.finish();
					Log.e(TAG, "login fail");
				}
			}
		};
	}

	private void gotoDeviceListActivity() {
		Intent intent = new Intent();
		intent.setClass(VideoLoginActivity.this, DeviceActivity.class);
		startActivity(intent);
		VideoLoginActivity.this.finish();
		Log.i(TAG, "login success");
	}

	private void closeWaitDoalog() {
		if (loginProcessDialog != null) {
			if (loginProcessDialog.isShowing()) {
				loginProcessDialog.dismiss();
				loginProcessDialog = null;
			}
		}
	}

	public static class MyHandler extends Handler {

	}

}
