package com.djc.logintest.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.baidu.android.pushservice.PushSettings;
import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.handler.PushEventHandler;
import com.djc.logintest.utils.MethodUtils;
import com.djc.logintest.utils.Utils;

public class MyService extends Service {
	private CheckNewsTask checkNewsTask;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("DJC 10-16", "MyService onCreate");
		PushSettings.enableDebugMode(this, true);
		PushEventHandler.getPushEventHandler().start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int command = getCommand(intent);

		switch (command) {
		case ConstantValue.COMMAND_TYPE_CHECK_NOTICE:
			Log.d("ddd", "onStartCommand COMMAND_TYPE_CHECK_NOTICE");
			checkNews();
			break;

		default:
			break;
		}

		return START_STICKY;
	}

	private int getCommand(Intent intent) {
		int command = -1;
		if (intent != null) {
			command = intent
					.getIntExtra(ConstantValue.COMMAND_CHECK_NOTICE, -1);
		}
		return command;
	}

	private void checkNews() {
		if (checkNewsTask == null
				|| checkNewsTask.getStatus() != AsyncTask.Status.RUNNING) {
			checkNewsTask = new CheckNewsTask();
			checkNewsTask.execute();
		} else {
			Log.d("djc", "should not getNewsImpl task already running!");
		}
	}

	class CheckNewsTask extends AsyncTask<Void, Void, Void> {
		boolean exist_news = false;

		@Override
		protected Void doInBackground(Void... params) {
			exist_news = MethodUtils.checkNews();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (exist_news) {
				Utils.saveProp(ConstantValue.HAVE_NEWS_NOTICE, "true");
				MyApplication instance = MyApplication.getInstance();
				if (instance != null) {
					instance.updateNotify(0, 0);
				}
				MethodUtils.setNewsNotification();
			}
		}
	}
}
