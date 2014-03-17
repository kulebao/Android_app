package com.djc.logintest.net;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.constant.ServerUrls;
import com.djc.logintest.httpclientmgr.HttpClientHelper;

public class ValidatePhoneMethod {
	private final static String CHECK_PHONE_RESULT = "check_phone_result";

	private ValidatePhoneMethod() {
	}

	public static ValidatePhoneMethod getMethod() {
		return new ValidatePhoneMethod();
	}

	public int validatePhone(String phone) {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		Log.e("DDDDD ", "createGetAuthCodeCommand cmd:"
				+ ServerUrls.CHECK_PHONE_NUM_URL);
		try {
			result = HttpClientHelper.executePost(
					ServerUrls.CHECK_PHONE_NUM_URL,
					getValidatePhoneCommand(phone));
			bret = handleValidateAuthCodeResult(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bret;
	}

	private String getValidatePhoneCommand(String phone) throws JSONException {
		JSONObject object = new JSONObject();
		object.put(JSONConstant.PHONE_NUM, phone);
		return object.toString();
	}

	private int handleValidateAuthCodeResult(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				event = EventType.PHONE_NUM_IS_INVALID;
				JSONObject object = result.getJsonObject();
				event = object.getInt(CHECK_PHONE_RESULT);
				Log.d("DDD ",
						"handleValidateAuthCodeResult str : "
								+ object.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return event;
	}
}
