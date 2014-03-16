package com.djc.logintest.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.constant.ServerUrls;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.SchoolInfo;
import com.djc.logintest.utils.Utils;

public class HttpsMethod {
	// 与服务器保持一致
	public final static String CHECK_PHONE_RESULT = "check_phone_result";

	public static HttpResult sendPostCommand(String url, String inCommand)
			throws IOException {
		HttpResult httpResult = new HttpResult();
		int status = HttpStatus.SC_UNAUTHORIZED;
		HttpsURLConnection connection = null;
		PrintWriter writer = null;
		BufferedReader reader = null;
		try {
			connection = HttpsModel.createHttpsConnection(url,
					ConstantValue.HTTP_POST);
			connection.connect();

			writer = new PrintWriter(connection.getOutputStream());
			writer.println(inCommand);
			writer.flush();
			writer.close();

			status = connection.getResponseCode();
			if (isHttpRequestOK(status)) {
				// 显示结果
				reader = new BufferedReader(new InputStreamReader(
						connection.getInputStream(), "UTF-8"));
				String line = null;
				StringBuffer buffer = new StringBuffer();
				while ((line = reader.readLine()) != null) {
					buffer.append(line);
				}
				httpResult.setContent(buffer.toString());
			}

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
			Utils.close(reader);
		}
		httpResult.setResCode(status);
		return httpResult;
	}

	public static boolean isHttpRequestOK(int status) {
		// 特殊情况，返回400也认为请求成功
		return (status == HttpStatus.SC_OK || status == HttpStatus.SC_BAD_REQUEST);
	}

	public static int validatePhone(String url, String phonenum)
			throws IOException {
		int bret = EventType.NET_WORK_INVALID;
		HttpsURLConnection connection = null;
		PrintWriter writer = null;

		try {
			connection = HttpsModel.createHttpsConnection(url,
					ConstantValue.HTTP_POST);
			connection.connect();

			writer = new PrintWriter(connection.getOutputStream());
			JSONObject object = new JSONObject();
			object.put(JSONConstant.PHONE_NUM, phonenum);
			writer.println(object.toString());
			writer.flush();
			writer.close();
			bret = getResult(connection);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return bret;
	}

	public static int getResult(HttpsURLConnection connection)
			throws IOException, UnsupportedEncodingException {
		int result = EventType.NET_WORK_INVALID;
		BufferedReader reader = null;
		try {
			int status = connection.getResponseCode();
			if (isHttpRequestOK(status)) {
				// 显示结果
				reader = new BufferedReader(new InputStreamReader(
						connection.getInputStream(), "UTF-8"));
				String line = null;
				StringBuffer buffer = new StringBuffer();
				while ((line = reader.readLine()) != null) {
					buffer.append(line);
				}

				result = getResult(buffer.toString());
			} else {
				result = EventType.SERVER_INNER_ERROR;
			}
		} finally {
			Utils.close(reader);
		}
		return result;
	}

	private static int getResult(String json) {
		int result = EventType.PHONE_NUM_IS_INVALID;
		try {
			JSONObject object = new JSONObject(json);
			result = object.getInt(CHECK_PHONE_RESULT);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	// 向服务器发送，从百度push服务器获取到的信息，作为后续推送的标识
	public static int sendBinfInfo(String phonenum, String userid,
			String channelid) {
		int ret = EventType.NET_WORK_INVALID;

		// 拼接为json格式
		String bindCommand = getBindCommand(phonenum, userid, channelid);
		HttpResult result = new HttpResult();
		try {
			result = sendPostCommand(ServerUrls.SEND_BIND_INFO_URL, bindCommand);
			ret = handleSendBinfInfoResult(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	private static int handleSendBinfInfoResult(HttpResult result) {
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

	private static String getBindCommand(String phonenum, String userid,
			String channelid) {
		JSONObject jsonObject = new JSONObject();

		try {
			jsonObject.put(JSONConstant.PHONE_NUM, phonenum);
			jsonObject.put(JSONConstant.USER_ID, userid);
			jsonObject.put(JSONConstant.CHANNEL_ID, channelid);
			jsonObject.put(JSONConstant.DEVICE_TYPE, "android");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

}
