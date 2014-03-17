package com.djc.logintest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.djc.logintest.R;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.handler.MyHandler;
import com.djc.logintest.push.PushModel;
import com.djc.logintest.taskmgr.LoadingTask;

public class LoadingActivity extends UmengStatisticsActivity {
	private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		initHandler();
		new LoadingTask(handler).execute();
		PushModel.getPushModel().enableDebug(true);
	}

	private void initHandler() {
		handler = new MyHandler(this, null) {
			@Override
			public void handleMessage(Message msg) {
				if (LoadingActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				
				switch (msg.what) {
				case EventType.LOADING_SUCCESS:
					break;
				case EventType.LOADING_TO_GUARD:
					goNextActivity(GuideActivity.class);
					break;
				case EventType.LOADING_TO_MAIN:
					goNextActivity(MainActivity.class);
					break;
				case EventType.LOADING_TO_VALIDATEPHONE:
					goNextActivity(ValidatePhoneNumActivity.class);
					break;
				default:
					break;
				}
			}
		};
	}

	private void goNextActivity(Class<?> toClass) {
		Intent intent = new Intent();
		intent.setClass(this, toClass);
		startActivity(intent);
		finish();
	}

	@Override
	public void onBackPressed() {
		return;
	}

}
