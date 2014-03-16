package com.djc.logintest.net;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.constant.ServerUrls;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.ChatInfo;
import com.djc.logintest.httpclientmgr.HttpClientHelper;
import com.djc.logintest.utils.Utils;

public class ChatMethod {

	private static final String MOST = "most";
	private static final String FROM = "from";
	private static final String TO = "to";

	private ChatMethod() {
	}

	public static ChatMethod getMethod() {
		return new ChatMethod();
	}

	public List<ChatInfo> getChatInfo(int most, long from, long to, String sort)
			throws Exception {
		List<ChatInfo> list = new ArrayList<ChatInfo>();
		HttpResult result = new HttpResult();
		String command = createGetChatInfoCommand(most, from, to, sort);
		Log.e("DDDDD ", "getSchoolInfo cmd:" + command);
		result = HttpClientHelper.executeGet(command);
		if (!result.isRequestOK()) {
			throw new Exception("request faile error =" + result.getResCode());
		}
		list = handleGetChatMethodResult(result);
		return list;
	}

	public List<ChatInfo> sendChat(String content, int lastid) throws Exception {
		List<ChatInfo> list = new ArrayList<ChatInfo>();
		HttpResult result = new HttpResult();
		String url = createSendChatUrl(lastid);
		Log.e("DDDDD ", "uploadChildInfo cmd:" + url + " content=" + content);
		result = HttpClientHelper.executePost(url, content);
		if (result.getResCode() != HttpStatus.SC_OK) {
			throw new Exception("request faile error =" + result.getResCode());
		}
		list = handleGetChatMethodResult(result);
		return list;
	}

	private String createSendChatUrl(int lastid) {
		String url = String.format(ServerUrls.SEND_CHAT, DataMgr.getInstance()
				.getSchoolID(), Utils.getProp(JSONConstant.ACCOUNT_NAME));

		if (lastid != 0) {
			url += "?retrieve_recent_from=" + lastid;
		}
		return url;
	}

	private List<ChatInfo> handleGetChatMethodResult(HttpResult result)
			throws JSONException {
		List<ChatInfo> list = new ArrayList<ChatInfo>();
		if (result.getResCode() == HttpStatus.SC_OK) {
			JSONArray array = result.getJSONArray();

			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				ChatInfo info = parseChatInfo(object);
				list.add(info);
			}
		}

		return list;
	}

	private ChatInfo parseChatInfo(JSONObject object) throws JSONException {
		ChatInfo info = new ChatInfo();
		String sender = object.getString(ChatInfo.SENDER);
		long timestamp = object.getLong(JSONConstant.TIME_STAMP);
		String body = object.getString(ChatInfo.CONTENT);
		int serverID = object.getInt("id");
		String icon_url = object.getString("image");

		info.setContent(body);
		info.setServer_id(serverID);
		info.setSender(sender);
		info.setTimestamp(timestamp);
		info.setIcon_url(icon_url);
		return info;
	}

	private String createGetChatInfoCommand(int most, long from, long to,
			String sort) {
		String cmd = String.format(ServerUrls.GET_CHAT, DataMgr.getInstance()
				.getSchoolID(), Utils.getProp(JSONConstant.ACCOUNT_NAME));
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
		// cmd += "&sort=" + sort;

		Log.d("DDD", "createGetChatInfoCommand cmd=" + cmd);
		return cmd;
	}
}
