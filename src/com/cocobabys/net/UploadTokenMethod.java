package com.cocobabys.net;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import com.cocobabys.constant.ServerUrls;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.upload.UploadFactory;

import android.text.TextUtils;
import android.util.Log;

public class UploadTokenMethod {
	// 获取七牛的上传token
	private UploadTokenMethod() {
	}

	public static UploadTokenMethod getMethod() {
		return new UploadTokenMethod();
	}

	public String getUploadToken(String key) throws Exception {
		String token = "";
		HttpResult result = new HttpResult();
		String command = createGetUploadTokenCommand(key);
		Log.e("DDDDD ", "getSchoolInfo cmd:" + command);
		result = HttpClientHelper.executeGet(command);
		token = handleGetUploadTokenResult(result);
		return token;
	}

	private String createGetUploadTokenCommand(String key) {
		String url = String.format(ServerUrls.GET_UPLOAD_TOKEN,
				UploadFactory.getUploadBucket());
		if (!TextUtils.isEmpty(key)) {
			url += "&key=" + key;
		}
		return url;
	}

	private String handleGetUploadTokenResult(HttpResult result) {
		String token = "";
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				JSONObject jsonObject = result.getJsonObject();
				token = jsonObject.getString("token");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return token;
	}

}
