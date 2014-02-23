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
import com.djc.logintest.dbmgr.info.ChildInfo;
import com.djc.logintest.httpclientmgr.HttpClientHelper;
import com.djc.logintest.utils.Utils;

public class GetChatMethod {

	private static final String MOST = "most";
	private static final String FROM = "from";
	private static final String TO = "to";

	private GetChatMethod() {
	}

	public static GetChatMethod getMethod() {
		return new GetChatMethod();
	}

	public List<ChatInfo> getChatInfo(int most, long from, long to,String sort)
			throws Exception {
		List<ChatInfo> list = new ArrayList<ChatInfo>();
		HttpResult result = new HttpResult();
		String command = createGetChatInfoCommand(most, from, to,sort);
		Log.e("DDDDD ", "getSchoolInfo cmd:" + command);
		result = HttpClientHelper.executeGet(command);
		list = handleGetChatMethodResult(result);
		return list;
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
		String sender = object.getString("sender");
		long timestamp = object.getLong(JSONConstant.TIME_STAMP);
		String body = object.getString("content");
		int serverID = object.getInt("id");
		String icon_url = object.getString("image");

		info.setContent(body);
		info.setServer_id(serverID);
		info.setSender(sender);
		info.setTimestamp(timestamp);
		info.setIcon_url(icon_url);
		return info;
	}

	private String createGetChatInfoCommand(int most, long from, long to,String sort) {
		if (most == 0) {
			most = ConstantValue.GET_CHATINFO_MAX_COUNT;
		}
		String cmd = String.format(ServerUrls.GET_CHAT, DataMgr
				.getInstance().getSchoolID(),Utils.getProp(JSONConstant.ACCOUNT_NAME),from,to,sort);

		Log.d("DDD", "createGetChatInfoCommand cmd=" + cmd);
		return cmd;
	}
}
