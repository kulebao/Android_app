package com.cocobabys.net;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.InfoHelper;
import com.cocobabys.dbmgr.info.ScheduleInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;

import android.util.Log;

public class ScheduleMethod {

	private ScheduleMethod() {
	}

	public static ScheduleMethod getMethod() {
		return new ScheduleMethod();
	}

	public int checkSchedule() throws Exception {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		String url = createSchedulePreviewUrl();
		Log.e("DDDDD ", "createGetChildrenInfoUrl cmd:" + url);
		result = HttpClientHelper.executeGet(url);
		bret = handleCheckScheduleResult(result);
		return bret;
	}

	private String createSchedulePreviewUrl() {
		String url = String.format(ServerUrls.SCHEDULE_PRIVIEW, DataMgr.getInstance().getSchoolID(), DataMgr
				.getInstance().getSelectedChild().getClass_id());
		return url;
	}

	private int handleCheckScheduleResult(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				if (result.isEmptyContent()) {
					return EventType.NO_SCHEDULE;
				}

				// 返回单一元素数组，保存最新课程表
				JSONArray array = result.getJSONArray();
				JSONObject jsonObject = array.getJSONObject(0);
				Log.d("DDD handleGetChildInfoResult", "str : " + jsonObject.toString());
				int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);
				if (errorcode == 0) {
					event = handleSuccess(jsonObject);
				} else {
					event = EventType.GET_SCHEDULE_FAILED;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			event = EventType.SERVER_INNER_ERROR;
		}

		return event;
	}

	private int handleSuccess(JSONObject jsonObject) throws JSONException {
		String newtimestamp = jsonObject.getString(InfoHelper.TIMESTAMP);
		long newTime = Long.parseLong(newtimestamp);
		ScheduleInfo scheduleInfo = DataMgr.getInstance().getScheduleInfo();

		if (scheduleInfo == null || (newTime > Long.parseLong(scheduleInfo.getTimestamp()))) {
			return getSchedule(jsonObject.getString(ScheduleInfo.SCHEDULE_ID));
		}
		return EventType.GET_SCHEDULE_LATEST;
	}

	private int getSchedule(String scheduleID) {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		String url = createGetScheduleUrl(scheduleID);
		Log.e("DDDDD ", "getSchedule cmd:" + url);
		try {
			result = HttpClientHelper.executeGet(url);
			bret = handleGetScheduleResult(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bret;
	}

	private int handleGetScheduleResult(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				// 返回单一元素数组，保存最新课程表
				JSONObject jsonObject = result.getJsonObject();
				Log.d("DDD handleGetChildInfoResult", "str : " + jsonObject.toString());
				int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);
				if (errorcode == 0) {
					saveSchedule(jsonObject);
					return EventType.GET_SCHEDULE_SUCCESS;
				} else {
					event = EventType.GET_SCHEDULE_FAILED;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			event = EventType.SERVER_INNER_ERROR;
		}
		return event;
	}

	private String createGetScheduleUrl(String scheduleID) {
		String url = String.format(ServerUrls.GET_SCHEDULE, DataMgr.getInstance().getSchoolID(), DataMgr.getInstance()
				.getSelectedChild().getClass_id(), scheduleID);
		return url;
	}

	private void saveSchedule(JSONObject jsonObject) throws JSONException {
		ScheduleInfo info = new ScheduleInfo();
		info.setTimestamp(jsonObject.getString(InfoHelper.TIMESTAMP));
		info.setSchedule_id(jsonObject.getString(ScheduleInfo.SCHEDULE_ID));
		info.setSchedule_content(jsonObject.getString(InfoHelper.WEEK_DETAIL));
		DataMgr.getInstance().updateScheduleInfo(info);
	}

}
