package com.djc.logintest.net;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.constant.ServerUrls;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.SchoolInfo;
import com.djc.logintest.httpclientmgr.HttpClientHelper;
import com.djc.logintest.utils.Utils;

public class PushMethod {

	private PushMethod() {
	}

	public static PushMethod getMethod() {
		return new PushMethod();
	}

	public int sendBinfInfo() {
		// 拼接为json格式
		int ret = EventType.NET_WORK_INVALID;
		try {
			String bindCommand = getBindCommand(
					Utils.getProp(JSONConstant.ACCOUNT_NAME),
					Utils.getUndeleteableProp(JSONConstant.USER_ID),
					Utils.getUndeleteableProp(JSONConstant.CHANNEL_ID));
			HttpResult result = new HttpResult();
			result = HttpClientHelper.executePost(
					ServerUrls.SEND_BIND_INFO_URL, bindCommand);
			ret = handleSendBinfInfoResult(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public int sendBinfInfo(String phonenum, String userid, String channelid)
			throws Exception {
		int ret = EventType.NET_WORK_INVALID;

		// 拼接为json格式
		String bindCommand = getBindCommand(phonenum, userid, channelid);
		HttpResult result = new HttpResult();
		result = HttpClientHelper.executePost(ServerUrls.SEND_BIND_INFO_URL,
				bindCommand);
		ret = handleSendBinfInfoResult(result);
		return ret;
	}

	private int handleSendBinfInfoResult(HttpResult result) {
		int event = EventType.NET_WORK_INVALID;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				JSONObject jsonObject = result.getJsonObject();
				Log.d("DDD LOGIN", "str : " + jsonObject.toString());

				int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);
				// 校验成功，保存token以及学校id(作为该设备的push tag)
				if (errorcode == 0) {
					String token = jsonObject
							.getString(JSONConstant.ACCESS_TOKEN);
					String accountname = jsonObject
							.getString(JSONConstant.ACCOUNT_NAME);
					String schoolid = jsonObject
							.getString(JSONConstant.SCHOOL_ID);
					String schoolname = jsonObject
							.getString(JSONConstant.SCHOOL_NAME);
					Utils.saveProp(JSONConstant.ACCESS_TOKEN, token);
					Utils.saveProp(JSONConstant.ACCOUNT_NAME, accountname);
					Utils.saveUndeleteableProp(accountname, "true");
					SchoolInfo info = new SchoolInfo();
					info.setSchool_id(schoolid);
					info.setSchool_name(schoolname);
					DataMgr.getInstance().addSchoolInfo(info);
					event = EventType.BIND_SUCCESS;
				} else {
					event = EventType.BIND_FAILED;
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return event;
	}

	private String getBindCommand(String phonenum, String userid,
			String channelid) {
		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(JSONConstant.PHONE_NUM, phonenum);
			jsonObject.put(JSONConstant.USER_ID, userid);
			jsonObject.put(JSONConstant.CHANNEL_ID, channelid);
			jsonObject.put(JSONConstant.DEVICE_TYPE, "android");
			jsonObject.put(JSONConstant.ACCESS_TOKEN,
					Utils.getProp(JSONConstant.ACCESS_TOKEN));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}
}
