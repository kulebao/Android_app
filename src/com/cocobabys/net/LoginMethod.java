package com.cocobabys.net;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.utils.DataUtils;

import android.util.Log;

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
					DataUtils.saveProp(JSONConstant.ACCESS_TOKEN, token);
					DataUtils.saveProp(JSONConstant.USERNAME, username);
					DataUtils.saveProp(JSONConstant.ACCOUNT_NAME, accountname);
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
