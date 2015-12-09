package com.cocobabys.net;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.InfoHelper;
import com.cocobabys.dbmgr.info.SchoolInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.utils.DataUtils;

public class SchoolMethod {

	private SchoolMethod() {
	}

	public static SchoolMethod getGetAuthCodeMethod() {
		return new SchoolMethod();
	}

	public void saveSchoolConfig() {
		HttpResult result = new HttpResult();
		try {
			result = HttpClientHelper.executeGet(String.format(ServerUrls.GET_SCHOOL_CONFIG, DataMgr.getInstance()
					.getSchoolID()));

			if (result.getResCode() == HttpStatus.SC_OK) {
				saveVideoProperty(result);
			} else {
				Log.e("getSchoolConfig failed", "result =" + result.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveVideoProperty(HttpResult result) throws JSONException {
		JSONObject jsonObject = result.getJsonObject();
		JSONArray jsonArray = jsonObject.getJSONArray("config");

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject2 = jsonArray.getJSONObject(i);
			if ("hideVideo".equals(jsonObject2.getString("name"))) {
				DataUtils.saveProp(JSONConstant.HIDE_VIDEO, jsonObject2.getString("value"));
				break;
			}

		}
	}

	public int checkSchoolInfo() throws Exception {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		String command = createCheckSchoolInfoCommand();
		Log.e("DDDDD ", "getSchoolInfo cmd:" + command);
		result = HttpClientHelper.executeGet(command);
		bret = handleCheckSchoolInfoResult(result);
		return bret;
	}

	private String createCheckSchoolInfoCommand() {
		return String.format(ServerUrls.GET_SCHOOL_PRIVIEW, DataMgr.getInstance().getSchoolID());
	}

	private int handleCheckSchoolInfoResult(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			event = EventType.SERVER_INNER_ERROR;
			try {
				JSONArray array = result.getJSONArray();
				JSONObject jsonObject = array.getJSONObject(0);

				Log.d("DDD handleCheckSchoolInfoResult", "str : " + jsonObject.toString());
				int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);

				// 登录成功，保存学校动态信息
				if (errorcode == 0) {
					long timestamp = Long.parseLong(jsonObject.getString(InfoHelper.TIMESTAMP));
					SchoolInfo schoolInfo = DataMgr.getInstance().getSchoolInfo();
					long oldtimestamp = schoolInfo.getTimestamp().equals("") ? -1 : Long.parseLong(schoolInfo
							.getTimestamp());
					// 信息没有更新，到此结束
					if (timestamp <= oldtimestamp) {
						event = EventType.SCHOOL_INFO_IS_LATEST;
					} else {
						// 信息更新，获取新信息
						event = getSchoolInfo();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			event = EventType.SERVER_INNER_ERROR;
		}

		return event;
	}

	private int getSchoolInfo() {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		String command = createGetSchoolInfoCommand();
		Log.e("DDDDD ", "getSchoolInfo cmd:" + command);
		try {
			result = HttpClientHelper.executeGet(command);
			Log.e("DDDDD ", "getSchoolInfo result:" + result.getContent());
			bret = handleGetSchoolInfoResult(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bret;
	}

	private String createGetSchoolInfoCommand() {
		return String.format(ServerUrls.GET_SCHOOL_DETAIL, DataMgr.getInstance().getSchoolID());
	}

	private int handleGetSchoolInfoResult(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			event = EventType.SERVER_INNER_ERROR;
			try {
				JSONObject jsonObject = result.getJsonObject();
				Log.d("DDD handleCheckSchoolInfoResult", "str : " + jsonObject.toString());
				SchoolInfo newInfo = SchoolInfo.jsonObjToChildInfo(jsonObject);
				// 单独解析school_id
				DataMgr.getInstance().updateSchoolInfo(DataMgr.getInstance().getSchoolID(), newInfo);
				event = EventType.UPDATE_SCHOOL_INFO;
				// }
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			event = EventType.SERVER_INNER_ERROR;
		}

		return event;
	}
}
