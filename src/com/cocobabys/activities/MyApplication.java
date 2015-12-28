package com.cocobabys.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.baidu.mapapi.SDKInitializer;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.NewChatInfo;
import com.cocobabys.handler.CrashHandler;
import com.cocobabys.im.IMHelper;
import com.cocobabys.im.SimpleConversationBahavior;
import com.cocobabys.media.MyMediaScannerConnectionClient;
import com.cocobabys.net.HttpsModel;
import com.cocobabys.push.info.PushEvent;
import com.cocobabys.receiver.NotificationObserver;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.provider.CameraInputProvider;
import io.rong.imkit.widget.provider.ImageInputProvider;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imlib.model.Conversation;

public class MyApplication extends Application {
	private static MyApplication instance;
	private static BlockingQueue<PushEvent> blockingQueue = new ArrayBlockingQueue<PushEvent>(
			ConstantValue.PUSH_ACTION_QUEUE_MAX_SIZE);

	private List<NotificationObserver> observers = new ArrayList<NotificationObserver>();
	private List<NewChatInfo> tmpNewChatList = new ArrayList<NewChatInfo>();
	private boolean forTest = false;

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

	/**
	 * 获得当前进程的名字
	 *
	 * @param context
	 * @return
	 */
	public static String getCurProcessName(Context context) {

		int pid = android.os.Process.myPid();

		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

		for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {

			if (appProcess.pid == pid) {

				return appProcess.processName;
			}
		}
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
		HttpsModel.initHttpsClient();
		instance = this;

		// 如果有数据库需要升级，则触发onUpgrade方法
		DataMgr.getInstance();
		Log.d("Database", "MyApplication onCreate");

		mediaScannerConnectionClient = new MyMediaScannerConnectionClient(this);
		SDKInitializer.initialize(this);

		initIM();
		initShareSDK();
	}

	private void initIM() {
		/**
		 *
		 * OnCreate 会被多个进程重入，这段保护代码，确保只有您需要使用 RongIM 的进程和 Push 进程执行了 init。
		 * io.rong.push 为融云 push 进程名称，不可修改。
		 */
		if (getApplicationInfo().packageName.equals(getCurProcessName(getApplicationContext()))
				|| "io.rong.push".equals(getCurProcessName(getApplicationContext()))) {
			/**
			 * IMKit SDK调用第一步 初始化
			 */

			if (forTest) {
				RongIM.init(this, "0vnjpoadnwk0z");
			} else {
				RongIM.init(this, "8w7jv4qb7tbqy");
			}

			IMHelper imHelper = new IMHelper();
			RongIM.setUserInfoProvider(imHelper, true);// 设置用户信息提供者。
			RongIM.setGroupInfoProvider(imHelper, true);// 设置群组信息提供者。
			RongIM.setOnReceiveMessageListener(imHelper);
			RongIM.setConversationBehaviorListener(new SimpleConversationBahavior());

			// 扩展功能自定义
			InputProvider.ExtendProvider[] provider = { new ImageInputProvider(RongContext.getInstance()), // 图片
					new CameraInputProvider(RongContext.getInstance()), // 相机
			};
			RongIM.resetInputExtensionProvider(Conversation.ConversationType.PRIVATE, provider);
			RongIM.resetInputExtensionProvider(Conversation.ConversationType.GROUP, provider);
			RongIM.resetInputExtensionProvider(Conversation.ConversationType.APP_PUBLIC_SERVICE, provider);
		}
	}

}
