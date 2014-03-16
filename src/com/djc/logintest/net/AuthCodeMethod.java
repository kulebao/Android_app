package com.djc.logintest.net;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.constant.ServerUrls;
import com.djc.logintest.httpclientmgr.HttpClientHelper;

public class AuthCodeMethod {
	private AuthCodeMethod() {
	}

	public static AuthCodeMethod getGetAuthCodeMethod() {
		return new AuthCodeMethod();
	}

	public int getAuthCode(String phone, int type) {
		int bret = EventType.GET_AUTH_CODE_FAIL;
		HttpResult result = new HttpResult();
		String url = createAuthCodeCommand(phone);
		Log.e("DDDDD ", "createGetAuthCodeCommand cmd:" + url);
		try {
			result = HttpClientHelper.executeGet(url);
			bret = handleGetAuthCodeResult(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bret;
	}

	public int validateAuthCode(String phone, String authcode) {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		String url = createAuthCodeCommand(phone);
		Log.e("DDDDD ", "createGetAuthCodeCommand cmd:" + url);
		try {
			result = HttpClientHelper.executePost(url,
					getAuthCommand(phone, authcode));
			bret = handleValidateAuthCodeResult(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bret;
	}

	private String getAuthCommand(String phonenum, String authCode) {
		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put("phone", phonenum);
			jsonObject.put(JSONConstant.AUTH_CODE, authCode);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	private String createAuthCodeCommand(String phone) {
		String url = String.format(ServerUrls.GET_AUTH_CODE_URL, phone);
		return url;
	}

	private int handleGetAuthCodeResult(HttpResult result) {
		int event = EventType.GET_AUTH_CODE_FAIL;
		if (result.isRequestOK()) {
			try {
				JSONObject jsonObject = result.getJsonObject();
				Log.d("DDD handleGetAuthCodeResult",
						"str : " + jsonObject.toString());

				int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);

				// 登录成功，保存token
				if (errorcode == 0) {
					event = EventType.GET_AUTH_CODE_SUCCESS;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return event;
	}

	private int handleValidateAuthCodeResult(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				JSONObject jsonObject = result.getJsonObject();
				Log.d("DDD handleValidateAuthCodeResult",
						"str : " + jsonObject.toString());

				int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);

				// 登录成功，保存token
				if (errorcode == 0) {
					event = EventType.AUTH_CODE_IS_VALID;
				} else {
					event = EventType.AUTH_CODE_IS_INVALID;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return event;
	}
}
