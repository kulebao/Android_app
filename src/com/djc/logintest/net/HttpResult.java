package com.djc.logintest.net;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.djc.logintest.constant.EventType;

public class HttpResult {
	private int resCode = EventType.NET_WORK_INVALID;
	// 返回的字符串流
	private String content = "";

	private static final String JSONARRAY_EMPTY_STR = new JSONArray().toString();
	private static final String JSONOBJ_EMPTY_STR = new JSONObject().toString();

	public int getResCode() {
		return resCode;
	}

	public boolean isEmptyContent() {
		return TextUtils.isEmpty(content) || JSONARRAY_EMPTY_STR.equals(content) || JSONOBJ_EMPTY_STR.equals(content);
	}

	public boolean isRequestOK() {
		return (resCode == HttpStatus.SC_OK || resCode == HttpStatus.SC_BAD_REQUEST);
	}

	public void setResCode(int resCode) {
		this.resCode = resCode;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public JSONObject getJsonObject() throws JSONException {
		return new JSONObject(content);
	}

	public JSONArray getJSONArray() throws JSONException {
		return new JSONArray(content);
	}
}
