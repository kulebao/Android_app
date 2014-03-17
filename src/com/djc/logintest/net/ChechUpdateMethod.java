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

public class ChechUpdateMethod {
	private ChechUpdateMethod() {
	}

	public static ChechUpdateMethod getChechUpdateMethod() {
		return new ChechUpdateMethod();
	}

	public int chechUpdate(int versionCode) throws Exception {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		String command = createCommand(versionCode);
		result = HttpClientHelper.executeGet(command);
		bret = handle(result);
		return bret;
	}

	private int handle(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				Log.d("DDD ChechUpdateMethod", "str : " + result.getContent());
				JSONObject jsonObject = result.getJsonObject();

				int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);

				// 保存版本更新信息
				if (errorcode == 0) {
					String updateUrl = jsonObject
							.getString(JSONConstant.UPDATE_URL);
					String updateContent = jsonObject
							.getString(JSONConstant.UPDATE_CONTENT);
					String versionName = jsonObject
							.getString(JSONConstant.UPDATE_VERSION_NAME);
					long size = jsonObject.getLong(JSONConstant.FILE_SIZE);
					Utils.saveProp(JSONConstant.UPDATE_URL, updateUrl);
					Utils.saveProp(JSONConstant.UPDATE_CONTENT, updateContent);
					Utils.saveProp(JSONConstant.UPDATE_VERSION_NAME,
							versionName);
					Utils.saveProp(JSONConstant.FILE_SIZE, String.valueOf(size));
					event = EventType.HAS_NEW_VERSION;
				} else {
					event = EventType.HAS_NO_VERSION;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return event;
	}

	private String createCommand(int versionCode) {
		String url = String.format(ServerUrls.CHECK_UPDATE, versionCode);
		return url;
	}
}
