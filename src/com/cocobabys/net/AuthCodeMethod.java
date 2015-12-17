package com.cocobabys.net;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.httpclientmgr.HttpClientHelper;

import android.util.Log;

public class AuthCodeMethod {
	private AuthCodeMethod() {
	}

	public static AuthCodeMethod getGetAuthCodeMethod() {
		return new AuthCodeMethod();
	}

	public int getAuthCode(String phone, int type) throws Exception {
		int bret = EventType.GET_AUTH_CODE_FAIL;
		HttpResult result = new HttpResult();
		String url = createAuthCodeCommand(phone);
		Log.e("DDDDD ", "createGetAuthCodeCommand cmd:" + url);
		result = HttpClientHelper.executeGet(url);
		bret = handleGetAuthCodeResult(result);
		return bret;
	}

	public int validateAuthCode(String phone, String authcode) throws Exception {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		String url = createAuthCodeCommand(phone);
		Log.e("DDDDD ", "createGetAuthCodeCommand cmd:" + url);
		result = HttpClientHelper.executePost(url,
				getAuthCommand(phone, authcode));
		bret = handleValidateAuthCodeResult(result);
		return bret;
	}

	private String getAuthCommand(String phonenum, String authCode) {
		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put("phone", phonenum);
			jsonObject.put("code", authCode);
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
				} else if (errorcode == 1) {
					event = EventType.GET_AUTH_CODE_TOO_OFTEN;
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
