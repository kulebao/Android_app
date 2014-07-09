package com.cocobabys.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.NewChatInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;

public class NewChatMethod {

	private static final String MOST = "most";
	private static final String FROM = "from";
	private static final String TO = "to";

	private NewChatMethod() {
	}

	public static NewChatMethod getMethod() {
		return new NewChatMethod();
	}

	public MethodResult getChatInfo(int most, long from, long to, String childid)
			throws Exception {
		MethodResult methodResult = new MethodResult(EventType.GET_CHAT_SUCCESS);

		List<NewChatInfo> list = new ArrayList<NewChatInfo>();
		HttpResult result = new HttpResult();
		String command = createGetChatInfoCommand(most, from, to, childid);
		Log.e("DDDDD ", "getSchoolInfo cmd:" + command);
		result = HttpClientHelper.executeGet(command);
		if (!result.isRequestOK()) {
			methodResult.setResultType(EventType.GET_CHAT_FAIL);
			return methodResult;
		}
		list = handleGetChatMethodResult(result);
		methodResult.setResultObj(list);
		return methodResult;
	}

	public MethodResult sendChat(String content, long lastid, String childid)
			throws Exception {
		MethodResult methodResult = new MethodResult(
				EventType.SEND_CHAT_SUCCESS);
		List<NewChatInfo> list = new ArrayList<NewChatInfo>();
		HttpResult result = new HttpResult();
		String url = createSendChatUrl(lastid, childid);
		Log.e("DDDDD ", "uploadChildInfo cmd:" + url + " content=" + content);
		result = HttpClientHelper.executePost(url, content);
		if (result.getResCode() != HttpStatus.SC_OK) {
			methodResult.setResultType(EventType.SEND_CHAT_FAIL);
			return methodResult;
		}
		list = handleGetChatMethodResult(result);
		// 返回顺序不对，貌似协议定错了。。。,这里反转一下
		if (list.size() > 1) {
			Collections.reverse(list);
		}
		methodResult.setResultObj(list);
		return methodResult;
	}

	public MethodResult deleteChat(long chatid, String childid)
			throws Exception {
		MethodResult methodResult = new MethodResult(
				EventType.DELETE_CHAT_SUCCESS);
		HttpResult result = new HttpResult();
		String url = createDeleteChatUrl(chatid, childid);
		Log.e("DDDDD ", "deleteChat cmd:" + url + " content=" + chatid);
		result = HttpClientHelper.executeDelete(url);
		Log.e("DDDDD ", " result =" + result.getContent());
		if (result.getResCode() != HttpStatus.SC_OK) {
			methodResult.setResultType(EventType.DELETE_CHAT_FAIL);
		}
		return methodResult;
	}

	private String createDeleteChatUrl(long chatid, String childid) {
		String url = String.format(ServerUrls.DELETE_CHAT, DataMgr
				.getInstance().getSchoolID(), childid, chatid);
		return url;
	}

	private String createSendChatUrl(long lastid, String childid) {
		String url = String.format(ServerUrls.SEND_NEW_CHAT, DataMgr
				.getInstance().getSchoolID(), childid);

		if (lastid != 0) {
			url += "?retrieve_recent_from=" + lastid;
		}
		return url;
	}

	private List<NewChatInfo> handleGetChatMethodResult(HttpResult result)
			throws JSONException {
		List<NewChatInfo> list = new ArrayList<NewChatInfo>();
		if (result.getResCode() == HttpStatus.SC_OK) {
			JSONArray array = result.getJSONArray();

			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				NewChatInfo info = NewChatInfo.parseFromJson(object);
				list.add(info);
			}
		}

		return list;
	}

	private String createGetChatInfoCommand(int most, long from, long to,
			String childid) {
		String cmd = String.format(ServerUrls.GET_NEW_CHAT, DataMgr
				.getInstance().getSchoolID(), childid);
		if (most == 0) {
			most = ConstantValue.GET_CHATINFO_MAX_COUNT;
		}

		cmd += MOST + "=" + most;
		if (from != 0) {
			cmd += "&" + FROM + "=" + from;
		}

		if (to != 0) {
			cmd += "&" + TO + "=" + to;
		}

		Log.d("DDD", "createGetChatInfoCommand cmd=" + cmd);
		return cmd;
	}
}
