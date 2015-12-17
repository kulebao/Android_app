package com.cocobabys.net;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.constant.MyErrorCode;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.httpclientmgr.HttpClientHelper;

import android.util.Log;

public class ValidatePhoneMethod {
	private final static String CHECK_PHONE_RESULT = "check_phone_result";

	private ValidatePhoneMethod() {
	}

	public static ValidatePhoneMethod getMethod() {
		return new ValidatePhoneMethod();
	}

	public int validatePhone(String phone) throws Exception {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		Log.e("DDDDD ", "createGetAuthCodeCommand cmd:"
				+ ServerUrls.CHECK_PHONE_NUM_URL);
		result = HttpClientHelper.executePost(ServerUrls.CHECK_PHONE_NUM_URL,
				getValidatePhoneCommand(phone));
		bret = handleValidateAuthCodeResult(result);
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
				JSONObject object = result.getJsonObject();
				event = object.getInt(CHECK_PHONE_RESULT);

				if (event != MyErrorCode.PHONE_VALID
						&& event != MyErrorCode.PHONE_EXPIRED) {
					event = EventType.PHONE_NUM_IS_INVALID;
				}
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
