package com.cocobabys.activities;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cocobabys.R;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.push.PushModel;
import com.cocobabys.taskmgr.LoadingTask;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

public class LoadingActivity extends UmengStatisticsActivity {
	private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		Log.d("Database", "LoadingActivity onCreate");
		initHandler();
		// initData();
		new LoadingTask(handler).execute();
		PushModel.getPushModel().enableDebug(
				MyApplication.getInstance().isForTest());
	}

	private void initData() {
		if (MyApplication.getInstance().isForTest()) {
			DataUtils.saveUndeleteableProp(JSONConstant.CHANNEL_ID, "133d");
			DataUtils.saveUndeleteableProp(JSONConstant.USER_ID,
					"963386802751977894");
			DataUtils.saveUndeleteableProp(ConstantValue.TEST_PHONE, "true");
			DataUtils.setGuided();
		}
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
