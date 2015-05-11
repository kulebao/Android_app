package com.cocobabys.jobs;

import android.os.Handler;
import android.util.Log;

import com.cocobabys.bean.VideoAccount;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.net.MethodResult;
import com.cocobabys.net.VideoMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;
import com.cocobabys.video.VideoApp;
import com.huamaitel.api.HMDefines;
import com.huamaitel.api.HMDefines.LoginServerInfo;
import com.huamaitel.api.HMDefines.UserInfo;
import com.huamaitel.api.HMJniInterface;

public class LoginVideoJob extends MyJob {
	private Handler handler;

	private static final String SERVER_ADDR = "www.seebaobei.com";
	// private static final String SERVER_ADDR = "www.huamaiyun.com";

	private static final short SERVER_PORT = 80;

	private boolean requestPublic = false;

	public LoginVideoJob(Handler handler) {
		this.handler = handler;
	}

	@Override
	public void run() {

		int event = EventType.VIDEO_LOGIN_FAIL;
		try {
			MyProxyImpl proxy = getParentProxy();
			MethodResult result = MethodUtils.getBindResult(proxy);
			event = result.getResultType();

			Log.d("", "login to parent info event =" + event);

			// 需求变更，如果发现家长没有注册视频账号，则提供公共示范账号给家长观看
			if (event == EventType.VIDEO_GET_INFO_NOT_REG) {
				proxy = getPublicProxy();
				result = MethodUtils.getBindResult(proxy);
				event = result.getResultType();
				requestPublic = true;
			}

			if (event == EventType.VIDEO_GET_INFO_SUCCESS) {
				VideoAccount account = (VideoAccount) result.getResultObj();
				// HMDefines.LoginServerInfo info = getInfo("cocbaby",
				// "13880498549");

				Log.d("EEE", "name =" + account.getAccountName());
				Log.d("EEE", "pwd =" + account.getPwd());
				HMDefines.LoginServerInfo info = getInfo(
						account.getAccountName(), account.getPwd());
				event = loginToHuamai(info);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			handler.sendEmptyMessage(event);
		}
	}

	private MyProxyImpl getPublicProxy() {
		MyProxy proxy = new MyProxy();
		MyProxyImpl parentAccountProxy = (MyProxyImpl) proxy
				.bind(new MyProxyImpl() {
					@Override
					public MethodResult handle() throws Exception {
						MethodResult result = VideoMethod.getMethod()
								.getPublicInfo();
						return result;
					}
				});
		return parentAccountProxy;
	}

	private MyProxyImpl getParentProxy() {
		MyProxy proxy = new MyProxy();
		MyProxyImpl parentAccountProxy = (MyProxyImpl) proxy
				.bind(new MyProxyImpl() {
					@Override
					public MethodResult handle() throws Exception {
						String parentid = DataMgr.getInstance()
								.getSelfInfoByPhone().getParent_id();
						MethodResult result = VideoMethod.getMethod().getInfo(
								parentid);
						return result;
					}
				});
		return parentAccountProxy;
	}

	private int loginToHuamai(LoginServerInfo info) {
		int event = EventType.VIDEO_LOGIN_FAIL;
		try {
			HMJniInterface jni = VideoApp.getJni();
			int result = 0;

			// step 1: Connect the server.
			// int serverId = jni.connectServer(info,
			// VideoApp.mLoginServerError);
			StringBuilder error = new StringBuilder();
			int serverId = jni.connectServer(info, error);
			// int serverId = jni.connectServer(info);
			if (serverId > 0) {
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
				if (requestPublic) {
					event = EventType.VIDEO_LOGIN_PUBLIC_SUCCESS;
				} else {
					event = EventType.VIDEO_LOGIN_SUCCESS;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return event;
	}

	private HMDefines.LoginServerInfo getInfo(String name, String password) {
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
