package com.djc.logintest.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.baidu.android.pushservice.PushSettings;
import com.djc.logintest.command.Command;
import com.djc.logintest.command.CommandFactory;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.handler.PushEventHandler;

public class MyService extends Service {

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
		try {
			int type = getCommand(intent);
			Command command = CommandFactory.getCommandFactory().createCommand(
					type);
			command.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return START_STICKY;
	}

	private int getCommand(Intent intent) {
		int command = -1;
		if (intent != null) {
			command = intent.getIntExtra(ConstantValue.CHECK_NEW_COMMAND, -1);
		}
		return command;
	}

}
