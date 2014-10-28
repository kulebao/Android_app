package com.cocobabys.net;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.utils.DataUtils;

public class ChechUpdateMethod {
	private ChechUpdateMethod() {
	}

	public static ChechUpdateMethod getChechUpdateMethod() {
		return new ChechUpdateMethod();
	}

	public int chechUpdate(int versionCode) throws Exception {
		HttpResult result = new HttpResult();
		String command = createCommand(versionCode);
		result = HttpClientHelper.executeGet(command);
		int bret = handle(result);
		return bret;
	}

	private int handle(HttpResult result) {
		int event = EventType.HAS_NO_VERSION;
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
					DataUtils.saveProp(JSONConstant.UPDATE_URL, updateUrl);
					DataUtils.saveProp(JSONConstant.UPDATE_CONTENT, updateContent);
					DataUtils.saveProp(JSONConstant.UPDATE_VERSION_NAME,
							versionName);
					DataUtils.saveProp(JSONConstant.FILE_SIZE, String.valueOf(size));
					event = EventType.HAS_NEW_VERSION;
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
