package com.djc.logintest.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.content.Intent;

import com.baidu.frontia.FrontiaApplication;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.dbmgr.info.ChatInfo;
import com.djc.logintest.handler.CrashHandler;
import com.djc.logintest.net.HttpsModel;
import com.djc.logintest.push.info.PushEvent;
import com.djc.logintest.receiver.NotificationObserver;
import com.djc.logintest.service.MyService;

public class MyApplication extends FrontiaApplication {
	private static MyApplication instance;
	private static BlockingQueue<PushEvent> blockingQueue = new ArrayBlockingQueue<PushEvent>(
			ConstantValue.PUSH_ACTION_QUEUE_MAX_SIZE);

	private List<NotificationObserver> observers = new ArrayList<NotificationObserver>();
	private List<ChatInfo> tmpList = new ArrayList<ChatInfo>();

	private boolean forAutoTest = true;

	public boolean isForAutoTest() {
		return forAutoTest;
	}

	public BlockingQueue<PushEvent> getBlockingQueue() {
		return blockingQueue;
	}

	public static MyApplication getInstance() {
		return instance;
	}

	public void addObserver(NotificationObserver notificationObserver) {
		observers.add(notificationObserver);
	}

	public void removeObserver(NotificationObserver notificationObserver) {
		observers.remove(notificationObserver);
	}

	public void updateNotify(int noticeType, int param) {
		for (NotificationObserver observer : observers) {
			observer.update(noticeType, param);
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
		HttpsModel.initHttpsClient();
		instance = this;
		Intent service = new Intent(instance, MyService.class);
		instance.startService(service);
	}

	public List<ChatInfo> getTmpList() {
		return tmpList;
	}

	public void setTmpList(List<ChatInfo> tmpList) {
		this.tmpList.clear();
		this.tmpList = tmpList;
	}
}
