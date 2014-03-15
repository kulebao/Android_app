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
import com.djc.logintest.dbmgr.info.News;
import com.djc.logintest.httpclientmgr.HttpClientHelper;
import com.djc.logintest.utils.MethodUtils;

public class GetNormalNewsMethod {

	private static final String MOST = "most";
	private static final String FROM = "from";
	private static final String TO = "to";

	private GetNormalNewsMethod() {
	}

	public static GetNormalNewsMethod getMethod() {
		return new GetNormalNewsMethod();
	}

	public List<News> getNormalNews(int most, long from, long to)
			throws Exception {
		List<News> list = new ArrayList<News>();
		HttpResult result = new HttpResult();
		String command = createNormalNoticeCommand(most, from, to);
		Log.e("DDDDD ", "getSchoolInfo cmd:" + command);
		result = HttpClientHelper.executeGet(command);
		list = handleGetNormalNewsResult(result);
		return list;
	}

	private List<News> handleGetNormalNewsResult(HttpResult result)
			throws JSONException {
		List<News> list = new ArrayList<News>();
		if (result.getResCode() == HttpStatus.SC_OK) {
			JSONArray array = result.getJSONArray();

			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				News info = parseNews(object);
				// NoticePaserHelper.setNormalParams(object, notice);
				list.add(info);
			}
		}

		return list;
	}

	private News parseNews(JSONObject object) throws JSONException {
		News info = new News();
		String title = object.getString("title");
		long timestamp = object.getLong(JSONConstant.TIME_STAMP);
		String body = object.getString("content");
		int serverID = object.getInt("news_id");
		int class_id = object.getInt("class_id");

		info.setClass_id(class_id);
		info.setContent(body);
		info.setNews_server_id(serverID);
		// info.setPublisher("测试djc");
		info.setTitle(title);
		info.setTimestamp(timestamp);
		info.setType(JSONConstant.NOTICE_TYPE_NORMAL);
		return info;
	}

	private String createNormalNoticeCommand(int most, long from, long to) {
		if (most == 0) {
			most = ConstantValue.GET_NORMAL_NOTICE_MAX_COUNT;
		}
		String cmd = String.format(ServerUrls.GET_NORMAL_NOTICE, DataMgr
				.getInstance().getSchoolID());

		cmd += MOST + "=" + most;
		if (from != 0) {
			cmd += "&" + FROM + "=" + from;
		}

		if (to != 0) {
			cmd += "&" + TO + "=" + to;
		}

		cmd += "&class_id=" + MethodUtils.getAllFormatedClassid();
		;

		Log.d("DDD", "createNormalNoticeCommand cmd=" + cmd);
		return cmd;
	}

}
