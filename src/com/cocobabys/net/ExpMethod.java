package com.cocobabys.net;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONException;

import android.util.Log;

import com.cocobabys.bean.GroupExpInfo;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ExpInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;

public class ExpMethod {

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

	public MethodResult getExpInfoByYearAndMonth(int year, String month) throws Exception {
		MethodResult methodResult = new MethodResult(EventType.GET_EXP_INFO_SUCCESS);
		HttpResult result = new HttpResult();
		String url = createGetExpInfoUrl(year, month);
		Log.d("DJC", "getExpInfoByYearAndMonth cmd:" + url);
		result = HttpClientHelper.executeGet(url);
		if (result.getResCode() != HttpStatus.SC_OK) {
			methodResult.setResultType(EventType.GET_EXP_INFO_FAIL);
			return methodResult;
		}

		handleGetExpInfoResult(result);
		return methodResult;
	}

	private void handleGetExpInfoResult(HttpResult result) throws JSONException {
		List<ExpInfo> list = ExpInfo.parseFromJsonArray(result.getJSONArray());
		DataMgr.getInstance().addExpDataList(list);
	}

	private String createGetExpInfoUrl(int year, String month) {
		String url = String.format(ServerUrls.GET_EXP_INFO, DataMgr.getInstance().getSchoolID(), DataMgr.getInstance()
				.getSelectedChild().getServer_id());

		url += "month=" + year + month;
		return url;
	}

	public MethodResult sendExp(String content) throws Exception {
		MethodResult methodResult = new MethodResult(EventType.POST_EXP_SUCCESS);
		HttpResult result = new HttpResult();
		String url = createSendExpUrl();
		Log.e("DJC", "uploadChildInfo cmd:" + url + " content=" + content);
		result = HttpClientHelper.executePost(url, content);
		if (result.getResCode() != HttpStatus.SC_OK) {
			methodResult.setResultType(EventType.POST_EXP_FAIL);
			return methodResult;
		}

		saveExpInfoToDB(result);
		return methodResult;
	}

	private void saveExpInfoToDB(HttpResult result) throws JSONException {
		ExpInfo info = ExpInfo.parseFromJsonObj(result.getJsonObject());
		List<ExpInfo> list = new ArrayList<ExpInfo>();
		list.add(info);
		DataMgr.getInstance().addExpDataList(list);
	}

	private String createSendExpUrl() {
		String url = String.format(ServerUrls.GET_EXP_INFO, DataMgr.getInstance().getSchoolID(), DataMgr.getInstance()
				.getSelectedChild().getServer_id());
		return url;
	}
}
