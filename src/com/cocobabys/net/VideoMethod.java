package com.cocobabys.net;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import android.util.Log;

import com.cocobabys.bean.VideoAccount;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.httpclientmgr.HttpClientHelper;

public class VideoMethod {
	private VideoMethod() {
	}

	public static VideoMethod getMethod() {
		return new VideoMethod();
	}

	// 获取公共示范幼儿园账号
	public MethodResult getPublicInfo() throws Exception {
		HttpResult result = new HttpResult();
		String command = createPublicCommand();
		Log.d("DDD VideoMethod getPublicInfo", " str : " + command);
		result = HttpClientHelper.executeGet(command);
		return getResult(result);
	}

	public MethodResult getInfo(String parentid) throws Exception {
		HttpResult result = new HttpResult();
		String command = createCommand(parentid);
		Log.d("DDD VideoMethod getInfo", " str : " + command);
		result = HttpClientHelper.executeGet(command);
		return getResult(result);
	}

	private MethodResult getResult(HttpResult result) throws Exception {
		MethodResult methodResult = new MethodResult(
				EventType.VIDEO_GET_INFO_FAIL);
		if (result.getResCode() == HttpStatus.SC_OK) {
			JSONObject jsonObject = result.getJsonObject();
			VideoAccount account = new VideoAccount();
			account.setAccountName(jsonObject.getString("account"));
			account.setPwd(jsonObject.getString("password"));
			Log.d("DDD", "getResult getInfo : " + account.toString());
			methodResult.setResultObj(account);
			methodResult.setResultType(EventType.VIDEO_GET_INFO_SUCCESS);
		} else if (result.getResCode() == HttpStatus.SC_NOT_FOUND) {
			methodResult.setResultType(EventType.VIDEO_GET_INFO_NOT_REG);
		}
		return methodResult;
	}

	private String createCommand(String parentid) {
		String cmd = String.format(ServerUrls.GET_VIDEO_INFO, DataMgr
				.getInstance().getSchoolID(), parentid);
		return cmd;
	}

	private String createPublicCommand() {
		String cmd = String.format(ServerUrls.GET_PUBLIC_VIDEO_INFO, DataMgr
				.getInstance().getSchoolID());
		return cmd;
	}
}
