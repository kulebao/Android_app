package com.cocobabys.net;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.constant.MyErrorCode;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.utils.DataUtils;

import android.util.Log;

public class ResetPWDMethod {
	private ResetPWDMethod() {
	}

	public static ResetPWDMethod getResetPwdMethod() {
		return new ResetPWDMethod();
	}

	// 在知道旧密码的情况下，修改密码
	public int changePwd(String oldPwd, String newPwd, String account)
			throws Exception {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		String command = createChangePWDCommand(oldPwd, newPwd, account);
		result = HttpClientHelper.executePost(ServerUrls.CHANGE_PWD_URL,
				command);
		bret = handleChangePwdResult(result);
		return bret;
	}

	// 在忘记密码的情况下，通过短信收到的校验码进行密码重置
	public int resetPwd(String authcode, String newPwd, String account)
			throws Exception {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		String command = createResetPWDCommand(authcode, newPwd, account);
		result = HttpClientHelper
				.executePost(ServerUrls.RESET_PWD_URL, command);
		bret = handleResetPwdResult(result);
		return bret;
	}

	private int handleResetPwdResult(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				JSONObject jsonObject = result.getJsonObject();
				Log.d("DDD changePwd", "str : " + jsonObject.toString());

				int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);

				// 登录成功，保存token
				if (errorcode == 0) {
					String token = jsonObject
							.getString(JSONConstant.ACCESS_TOKEN);
					DataUtils.saveProp(JSONConstant.ACCESS_TOKEN, token);
					event = EventType.RESET_PWD_SUCCESS;
				} else if (errorcode == MyErrorCode.INVALID_AUTHCODE) {
					event = EventType.AUTH_CODE_IS_INVALID;
				} else {
					event = EventType.RESET_PWD_FAILED;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return event;
	}

	private int handleChangePwdResult(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				JSONObject jsonObject = result.getJsonObject();
				Log.d("DDD changePwd", "str : " + jsonObject.toString());

				int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);

				// 登录成功，保存token
				if (errorcode == 0) {
					String token = jsonObject
							.getString(JSONConstant.ACCESS_TOKEN);
					DataUtils.saveProp(JSONConstant.ACCESS_TOKEN, token);
					event = EventType.CHANGE_PWD_SUCCESS;
				} else {
					event = EventType.OLD_PWD_NOT_EQUAL;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return event;
	}

	private String createChangePWDCommand(String oldPwd, String newPwd,
			String account) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(JSONConstant.ACCOUNT_NAME, account);
			jsonObject.put(JSONConstant.OLD_PASSWORD, oldPwd);
			jsonObject.put(JSONConstant.NEW_PASSWORD, newPwd);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	private String createResetPWDCommand(String authcode, String newPwd,
			String account) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(JSONConstant.ACCOUNT_NAME, account);
			jsonObject.put(JSONConstant.AUTH_CODE, authcode);
			jsonObject.put(JSONConstant.NEW_PASSWORD, newPwd);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}
}
