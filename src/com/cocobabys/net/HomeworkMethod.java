package com.cocobabys.net;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.Homework;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.utils.MethodUtils;

public class HomeworkMethod {

	private static final String MOST = "most";
	private static final String FROM = "from";
	private static final String TO = "to";

	private HomeworkMethod() {
	}

	public static HomeworkMethod getMethod() {
		return new HomeworkMethod();
	}

	public List<Homework> getGetHomework(int most, long from, long to)
			throws Exception {
		List<Homework> list = new ArrayList<Homework>();
		HttpResult result = new HttpResult();
		String command = createGetHomeworkCommand(most, from, to);
		Log.e("DDDDD ", "getGetHomework cmd:" + command);
		result = HttpClientHelper.executeGet(command);
		list = handleGetHomeworkResult(result);
		return list;
	}

	private List<Homework> handleGetHomeworkResult(HttpResult result)
			throws JSONException {
		List<Homework> list = new ArrayList<Homework>();
		if (result.getResCode() == HttpStatus.SC_OK) {
			JSONArray array = result.getJSONArray();

			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				Homework info = parseHomework(object);
				list.add(info);
			}
		}

		return list;
	}

	private Homework parseHomework(JSONObject object) throws JSONException {
		Homework info = new Homework();
		String title = object.getString("title");
		long timestamp = object.getLong(JSONConstant.TIME_STAMP);
		String body = object.getString("content");
		int serverID = object.getInt("id");
		String publisher = object.getString("publisher");
		String icon_url = object.getString("icon_url");
		int class_id = object.getInt("class_id");

		info.setContent(body);
		info.setServer_id(serverID);
		info.setPublisher(publisher);
		info.setTitle(title);
		info.setTimestamp(timestamp);
		info.setIcon_url(icon_url);
		info.setClass_id(class_id);
		return info;
	}

	private String createGetHomeworkCommand(int most, long from, long to) {
		if (most == 0) {
			most = ConstantValue.GET_NORMAL_NOTICE_MAX_COUNT;
		}
		String cmd = String.format(ServerUrls.GET_HOMEWORK, DataMgr
				.getInstance().getSchoolID());

		cmd += MOST + "=" + most;
		if (from != 0) {
			cmd += "&" + FROM + "=" + from;
		}

		if (to != 0) {
			cmd += "&" + TO + "=" + to;
		}

		cmd += "&" + "class_id=" + MethodUtils.getAllFormatedClassid();

		Log.d("DDD", "createGetHomeworkCommand cmd=" + cmd);
		return cmd;
	}
}
