package com.djc.logintest.net;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.constant.ServerUrls;
import com.djc.logintest.httpclientmgr.HttpClientHelper;
import com.djc.logintest.utils.Utils;

public class LoginMethod {
	private LoginMethod() {
	}

	public static LoginMethod getLoginMethod() {
		return new LoginMethod();
	}

	public int login(String pwd, String account) throws Exception {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		String command = createCommand(pwd, account);
		result = HttpClientHelper.executePost(ServerUrls.LOGIN_URL, command);
		bret = handle(result);
		return bret;
	}

	private int handle(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				Log.d("DDD LOGIN", " str : " + result.getContent());
				JSONObject jsonObject = result.getJsonObject();

				int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);

				// 登录成功，保存token
				if (errorcode == 0) {
					String token = jsonObject
							.getString(JSONConstant.ACCESS_TOKEN);
					String username = jsonObject
							.getString(JSONConstant.USERNAME);
					String accountname = jsonObject
							.getString(JSONConstant.ACCOUNT_NAME);
					Utils.saveProp(JSONConstant.ACCESS_TOKEN, token);
					Utils.saveProp(JSONConstant.USERNAME, username);
					Utils.saveProp(JSONConstant.ACCOUNT_NAME, accountname);
					event = EventType.LOGIN_SUCCESS;
				} else {
					event = EventType.PWD_INCORRECT;
				}
			} catch (JSONException e) {
				event = EventType.SERVER_BUSY;
				e.printStackTrace();
			}
		}

		return event;
	}

	private String createCommand(String pwd, String account) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(JSONConstant.ACCOUNT_NAME, account);
			jsonObject.put(JSONConstant.PASSWORD, pwd);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}
}
