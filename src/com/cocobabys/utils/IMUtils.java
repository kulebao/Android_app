package com.cocobabys.utils;

import org.apache.http.HttpStatus;

import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.net.HttpResult;
import com.cocobabys.threadpool.MyThreadPoolMgr;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;;

public class IMUtils {

	// true表示免打扰打开，false表示免打扰关闭
	public static boolean isMessageDisturbEnable(String conversationID) {
		Context context = MyApplication.getInstance().getApplicationContext();
		SharedPreferences conf = context.getSharedPreferences(ConstantValue.CONF_INI, Context.MODE_PRIVATE);
		return conf.getBoolean(conversationID, false);
	}

	public static void setMessageDisturbEnable(String conversationID, boolean enable) {
		SharedPreferences.Editor editor = DataUtils.getEditor();
		editor.putBoolean(conversationID, enable);
		editor.commit();
	}

	public static void saveToken(String token) {
		SharedPreferences.Editor editor = DataUtils.getEditor();
		editor.putString(ConstantValue.IM_TOKEN, token);
		editor.commit();
	}

	public static String getToken() {
		Context context = MyApplication.getInstance().getApplicationContext();
		SharedPreferences conf = context.getSharedPreferences(ConstantValue.CONF_INI, Context.MODE_PRIVATE);
		return conf.getString(ConstantValue.IM_TOKEN, "");
	}

	/**
	 * 重连
	 *
	 * @param token
	 */
	public static void connect(final String token) {

		MyApplication instance = MyApplication.getInstance();
		if (instance.getApplicationInfo().packageName
				.equals(MyApplication.getCurProcessName(instance.getApplicationContext()))) {

			// RongIMClient.ConnectCallback connectCallback = new
			// RongIMClient.ConnectCallback() {
			// @Override
			// public void onTokenIncorrect() {
			// Log.e("", "reconnect token invalid :" + token);
			// MyThreadPoolMgr.getGenericService().execute(new Runnable() {
			// @Override
			// public void run() {
			// refreshToken();
			// }
			// });
			// }
			//
			// @Override
			// public void onSuccess(String userid) {
			// Log.d("", "connect onSuccess token:" + token);
			// Log.d("", "connect onSuccess userid:" + userid);
			// }
			//
			// @Override
			// public void onError(RongIMClient.ErrorCode errorCode) {
			// Log.e("", "reconnect error :" + errorCode);
			// }
			// };

			RongIM.connect(token, new MyRongIMConnectCallback());
		}
	}

	private static String createRefreshTokenCommand(String id) {
		String cmd = String.format(ServerUrls.REFRESH_IM_TOKEN_URL, DataMgr.getInstance().getSchoolID(), id);
		return cmd;
	}

	public static void refreshToken() {
		ParentInfo parent = DataMgr.getInstance().getSelfInfoByPhone();
		// String imUserid = parent.getIMUserid();
		int internal_id = parent.getInternal_id();
		Log.d("", "refreshToken internal_id =" + internal_id);
		String command = createRefreshTokenCommand(internal_id + "");
		Log.d("", "refreshToken command =" + command);

		try {
			HttpResult result = HttpClientHelper.executeGet(command);
			Log.d("", "refreshToken executeGet =" + result.getContent());
			if (result.getResCode() == HttpStatus.SC_OK) {
				String token = result.getJsonObject().getString("token");
				IMUtils.saveToken(token);
				Log.d("", "refreshToken connect again! token=" + token);
				RongIM.connect(token, new MyRongIMConnectCallback());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class MyRongIMConnectCallback extends RongIMClient.ConnectCallback {
		@Override
		public void onTokenIncorrect() {
			Log.e("", "reconnect token invalid");
			MyThreadPoolMgr.getGenericService().execute(new Runnable() {
				@Override
				public void run() {
					refreshToken();
				}
			});
		}

		@Override
		public void onSuccess(String userid) {
			Log.d("", "connect onSuccess token");
			Log.d("", "connect onSuccess  userid:" + userid);
		}

		@Override
		public void onError(RongIMClient.ErrorCode errorCode) {
			Log.e("", "reconnect error :" + errorCode);
		}
	}

}
