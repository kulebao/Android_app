package com.cocobabys.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

import com.baidu.mapapi.SDKInitializer;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.NewChatInfo;
import com.cocobabys.handler.CrashHandler;
import com.cocobabys.media.MyMediaScannerConnectionClient;
import com.cocobabys.net.HttpsModel;
import com.cocobabys.push.info.PushEvent;
import com.cocobabys.receiver.NotificationObserver;
import com.cocobabys.service.MyService;

public class MyApplication extends Application {
	private static MyApplication instance;
	private static BlockingQueue<PushEvent> blockingQueue = new ArrayBlockingQueue<PushEvent>(
			ConstantValue.PUSH_ACTION_QUEUE_MAX_SIZE);

	private List<NotificationObserver> observers = new ArrayList<NotificationObserver>();
	private List<NewChatInfo> tmpNewChatList = new ArrayList<NewChatInfo>();
	private boolean forTest = true;

	private MyMediaScannerConnectionClient mediaScannerConnectionClient;

	public MyMediaScannerConnectionClient getMediaScannerConnectionClient() {
		return mediaScannerConnectionClient;
	}

	// 当前是否有数据库正在升级
	private boolean isDbUpdating = false;

	private boolean weixinBypass = false;

	public boolean isWeixinBypass() {
		return weixinBypass;
	}

	public boolean isDbUpdating() {
		return isDbUpdating;
	}

	public void setDbUpdating(boolean isDbUpdating) {
		this.isDbUpdating = isDbUpdating;
	}

	public List<NewChatInfo> getTmpNewChatList() {
		return tmpNewChatList;
	}

	public void setTmpNewChatList(List<NewChatInfo> tmpNewChatList) {
		this.tmpNewChatList = tmpNewChatList;
	}

	public boolean isForTest() {
		return forTest;
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

	private void initShareSDK() {
		try {
			if (isWeixinBypass()) {
				ShareSDK.initSDK(this);
			} else {
				ShareSDK.initSDK(this, "77da60e4dcd8");
				HashMap<String, Object> hashMap = new HashMap<String, Object>();
				hashMap.put("Id", "4");
				hashMap.put("SortId", "4");
				hashMap.put("AppId", "wxf3c9e8b20267320e");
				hashMap.put("AppSecret", "b8058fb1aac2bac635332ea20679861b");
				hashMap.put("BypassApproval", "false");
				hashMap.put("Enable", "true");
				ShareSDK.setPlatformDevInfo(Wechat.NAME, hashMap);

				hashMap = new HashMap<String, Object>();
				hashMap.put("Id", "5");
				hashMap.put("SortId", "5");
				hashMap.put("AppId", "wxf3c9e8b20267320e");
				hashMap.put("AppSecret", "b8058fb1aac2bac635332ea20679861b");
				hashMap.put("BypassApproval", "false");
				hashMap.put("Enable", "true");

				ShareSDK.setPlatformDevInfo(WechatMoments.NAME, hashMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		// 如果有数据库需要升级，则触发onUpgrade方法
		DataMgr.getInstance();
		Log.d("Database", "MyApplication onCreate");

		mediaScannerConnectionClient = new MyMediaScannerConnectionClient(this);
		SDKInitializer.initialize(this);

		initShareSDK();
	}

}
