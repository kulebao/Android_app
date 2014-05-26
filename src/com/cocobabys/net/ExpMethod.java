package com.cocobabys.net;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.cocobabys.bean.GroupExpInfo;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ChatInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;

public class ExpMethod {

	private static final String MOST = "most";
	private static final String FROM = "from";
	private static final String TO = "to";

	private ExpMethod() {
	}

	public static ExpMethod getMethod() {
		return new ExpMethod();
	}

	public MethodResult getExpCount(int year) throws Exception {
		MethodResult methodResult = new MethodResult(EventType.GET_EXP_COUNT_SUCCESS);
		HttpResult result = new HttpResult();
		String url = createGetExpCountUrl(year);
		Log.d("DJC", "getExpCount cmd:" + url);
		result = HttpClientHelper.executeGet(url);
		if (result.getResCode() != HttpStatus.SC_OK) {
			methodResult.setResultType(EventType.GET_EXP_COUNT_FAIL);
			return methodResult;
		}
		List<GroupExpInfo> list = handleGetExpCountResult(result);
		methodResult.setResultObj(list);
		return methodResult;
	}

	private String createGetExpCountUrl(int year) {
		String url = String.format(ServerUrls.GET_EXP_COUNT, DataMgr.getInstance().getSchoolID(), DataMgr.getInstance()
				.getSelectedChild().getServer_id());
		url += "year=" + year;
		return url;
	}

	private List<GroupExpInfo> handleGetExpCountResult(HttpResult result) throws JSONException {
		return GroupExpInfo.jsonArrayToGroupExpInfoList(result.getJSONArray());
	}
}
