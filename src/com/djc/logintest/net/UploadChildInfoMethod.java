package com.djc.logintest.net;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.constant.ServerUrls;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.ChildInfo;
import com.djc.logintest.httpclientmgr.HttpClientHelper;
import com.djc.logintest.utils.Utils;

public class UploadChildInfoMethod {

	private UploadChildInfoMethod() {
	}

	public static UploadChildInfoMethod getMethod() {
		return new UploadChildInfoMethod();
	}

	public int uploadChildInfo(String content) throws Exception {
		int bret = EventType.NET_WORK_INVALID;
		HttpResult result = new HttpResult();
		String url = createUploadChildInfoUrl();
		Log.e("DDDDD ", "uploadChildInfo cmd:" + url);
		Log.e("DDDDD ", "uploadChildInfo content:" + content);
		result = HttpClientHelper.executePost(url, content);
		bret = handleGetChildInfoResult(result);
		return bret;
	}

	private String createUploadChildInfoUrl() {
		String url = String.format(ServerUrls.UPLOAD_CHILD_INFO, DataMgr
				.getInstance().getSchoolID(), DataMgr.getInstance()
				.getSelectedChild().getServer_id());
		return url;
	}

	private int handleGetChildInfoResult(HttpResult result) {
		int event = EventType.SERVER_BUSY;
		if (result.getResCode() == HttpStatus.SC_OK) {
			try {
				JSONObject jsonObject = result.getJsonObject();
				Log.d("DDD handleGetChildInfoResult",
						"str : " + jsonObject.toString());
				int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);
				// 登录成功，保存token
				if (errorcode == 0) {
					event = checkUpdate(jsonObject);
				} else {
					event = EventType.UPLOAD_FAILED;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			event = EventType.SERVER_INNER_ERROR;
		}

		return event;
	}

	private int checkUpdate(JSONObject jsonObject) throws JSONException {
		int event = EventType.SERVER_BUSY;
		String jsonSrc = jsonObject.getString("child_info");
		JSONObject obj = new JSONObject(jsonSrc);
		ChildInfo selectedChild = DataMgr.getInstance().getSelectedChild();
		if (selectedChild != null) {
			// 只对选中小孩进行判断是否更新
			ChildInfo childinfo = ChildInfo.jsonObjToChildInfo(obj);
			if (childinfo.getServer_id().equals(selectedChild.getServer_id())) {
				childinfo.setSelected(ChildInfo.STATUS_SELECTED);
				DataMgr.getInstance().updateChildInfo(childinfo.getServer_id(),
						childinfo);
				event = EventType.UPLOAD_SUCCESS;
			} else {
				// 返回小孩id错误
				Log.e("DDD", "bad child id!!!");
			}
		}
		return event;
	}

}
