package com.cocobabys.jobs;

import android.os.Handler;

import com.cocobabys.bean.VideoAccount;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.net.MethodResult;
import com.cocobabys.net.VideoMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.video.VideoApp;
import com.huamaitel.api.HMDefines;
import com.huamaitel.api.HMDefines.UserInfo;
import com.huamaitel.api.HMJniInterface;

public class LoginVideoJob extends MyJob {
	private Handler handler;
	private String name;
	private String password;

	private static final String SERVER_ADDR = "www.seebaobei.com";
	// private static final String SERVER_ADDR = "www.huamaiyun.com";

	private static final short SERVER_PORT = 80;

	public LoginVideoJob(Handler handler, String name, String password) {
		this.handler = handler;
		this.name = name;
		this.password = password;
	}

	@Override
	public void run() {
		int event = EventType.VIDEO_LOGIN_FAIL;

		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public MethodResult handle() throws Exception {
				String parentid = DataMgr.getInstance().getSelfInfoByPhone()
						.getParent_id();
				MethodResult result = VideoMethod.getMethod().getInfo(parentid);
				return result;
			}
		});

		try {
			MethodResult result = (MethodResult) bind.handle();
			event = result.getResultType();

			if (event == EventType.VIDEO_GET_INFO_SUCCESS) {
				VideoAccount account = (VideoAccount) result.getResultObj();
				event = loginToHuamai();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			handler.sendEmptyMessage(event);
		}
	}

	private int loginToHuamai() {
		int event = EventType.VIDEO_LOGIN_FAIL;
		try {
			HMJniInterface jni = VideoApp.getJni();
			int result = 0;

			HMDefines.LoginServerInfo info = getInfo();

			// step 1: Connect the server.
			// int serverId = jni.connectServer(info,
			// VideoApp.mLoginServerError);
			int serverId = jni.connectServer(info);
			if (serverId != 0) {
				VideoApp.serverId = serverId;
				result = jni.getDeviceList(serverId);
				if (result != HMDefines.HMEC_OK) {
					jni.disconnectServer(serverId);
					return event;
				}

				// step 2: Get user information.
				UserInfo userInfo = jni.getUserInfo(serverId);
				if (userInfo == null) {
					jni.disconnectServer(serverId);
					return event;
				}
				/**
				 * TODO: huamaiyun和see1000中需要添加userInfo.useTransferService !=8
				 * 
				 * 这个判断 seebao中需要去掉，否则会报错！
				 */
				// step 3: Get transfer service.
				// if (userInfo.useTransferService !=
				// 0&&userInfo.useTransferService !=8) {
				if (userInfo.useTransferService != 0) {
					result = jni.getTransferInfo(serverId);
					if (result != HMDefines.HMEC_OK) {
						jni.disconnectServer(serverId);
						return event;
					}
				}

				// step 4: Get tree id.
				VideoApp.treeId = jni.getTree(serverId);
				event = EventType.VIDEO_LOGIN_SUCCESS;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return event;
	}

	private HMDefines.LoginServerInfo getInfo() {
		// 平台相关
		HMDefines.LoginServerInfo info = new HMDefines.LoginServerInfo();
		info.ip = SERVER_ADDR; // 平台地址
		info.port = SERVER_PORT; // 平台端口
		info.user = name; // 用户名
		info.password = password; // 密码
		info.model = android.os.Build.MODEL; // 手机型号
		info.version = android.os.Build.VERSION.RELEASE; // 手机系统版本号
		return info;
	}

}
