package com.cocobabys.net;

import org.apache.http.HttpStatus;

import android.util.Log;

import com.cocobabys.bean.FullParentInfo;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.httpclientmgr.HttpClientHelper;

public class ParentMethod {

	private ParentMethod() {
	}

	public static ParentMethod getMethod() {
		return new ParentMethod();
	}

	private String createCommand(String phone) {
		String url = String.format(ServerUrls.UPDATE_PARENT_URL, DataMgr.getInstance().getSchoolID(), phone);
		return url;
	}

	public MethodResult updateParent(FullParentInfo parentInfo) throws Exception {
		HttpResult result = new HttpResult();
		MethodResult methodResult = new MethodResult(EventType.UPDATE_PARENT_FAIL);
		String command = createCommand(parentInfo.getPhone());

		String content = com.alibaba.fastjson.JSONObject.toJSONString(parentInfo);

		Log.d("DJC", "updateParent cmd:" + command + " content=" + content);
		result = HttpClientHelper.executePost(command, content);

		Log.d("DJC", "updateParent result:" + result);
		if (result.getResCode() == HttpStatus.SC_OK) {
			methodResult.setResultType(EventType.UPDATE_PARENT_SUCCESS);
			DataMgr.getInstance().updateParentInfo(parentInfo, parentInfo.getParent_id());
		}

		return methodResult;
	}

	public FullParentInfo getParent(String phone) throws Exception {
		FullParentInfo info = null;
		HttpResult result = new HttpResult();
		String command = createCommand(phone);

		Log.d("DJC", "getParent cmd:" + command);
		result = HttpClientHelper.executeGet(command);
		Log.d("DJC", "getParent result:" + result);

		if (result.getResCode() == HttpStatus.SC_OK) {
			info = com.alibaba.fastjson.JSONObject.parseObject(result.getContent(), FullParentInfo.class);
		}

		return info;
	}

}
