package com.cocobabys.net;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.SchoolInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.utils.DataUtils;

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
					DataUtils.getProp(JSONConstant.ACCOUNT_NAME),
					DataUtils.getUndeleteableProp(JSONConstant.USER_ID),
					DataUtils.getUndeleteableProp(JSONConstant.CHANNEL_ID));
			HttpResult result = new HttpResult();
			Log.d("DJC", "bindCommand =" + bindCommand);
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

		// 测试，当前服务器只会用userid，将channelid作为usrid传入
		// String bindCommand = getBindCommand(phonenum, channelid, userid);

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
					String member_status = jsonObject
							.getString(JSONConstant.MEMBER_STATUS);

					DataUtils.saveProp(JSONConstant.ACCESS_TOKEN, token);
					DataUtils.saveProp(JSONConstant.ACCOUNT_NAME, accountname);
					DataUtils.saveProp(JSONConstant.MEMBER_STATUS,
							member_status);
					DataUtils.saveUndeleteableProp(accountname, "true");

					SchoolInfo info = new SchoolInfo();
					info.setSchool_id(schoolid);
					info.setSchool_name(schoolname);
					DataMgr.getInstance().addSchoolInfo(info);
					event = EventType.BIND_SUCCESS;
				} else if (errorcode == 2) {
					event = EventType.PHONE_NUM_IS_INVALID;
				} else if (errorcode == 3) {
					event = EventType.PHONE_NUM_IS_ALREADY_LOGIN;
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
					DataUtils.getProp(JSONConstant.ACCESS_TOKEN));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}
}
