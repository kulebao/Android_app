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
import com.djc.logintest.dbmgr.info.ChildInfo;
import com.djc.logintest.dbmgr.info.Homework;
import com.djc.logintest.httpclientmgr.HttpClientHelper;

public class GetHomeworkMethod {

	private static final String MOST = "most";
	private static final String FROM = "from";
	private static final String TO = "to";

	private GetHomeworkMethod() {
	}

	public static GetHomeworkMethod getMethod() {
		return new GetHomeworkMethod();
	}

	public List<Homework> getGetHomework(int most, long from, long to)
			throws Exception {
		List<Homework> list = new ArrayList<Homework>();
		HttpResult result = new HttpResult();
		String command = createGetHomeworkCommand(most, from, to);
		Log.e("DDDDD ", "getSchoolInfo cmd:" + command);
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

		info.setContent(body);
		info.setServer_id(serverID);
		info.setPublisher(publisher);
		info.setTitle(title);
		info.setTimestamp(timestamp);
		info.setIcon_url(icon_url);
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

		cmd += "&" + "classId=" + getAllClassid();

		Log.d("DDD", "createGetHomeworkCommand cmd=" + cmd);
		return cmd;
	}

	private String getAllClassid() {
		String classIDs = "";
		List<ChildInfo> allChildrenInfo = DataMgr.getInstance()
				.getAllChildrenInfo();
		for (ChildInfo childInfo : allChildrenInfo) {
			classIDs += childInfo.getClass_id() + ",";
		}
		return classIDs.substring(0, classIDs.length() - 1);
	}
}
