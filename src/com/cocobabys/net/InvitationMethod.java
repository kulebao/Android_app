package com.cocobabys.net;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.utils.DataUtils;

public class InvitationMethod {
	private static final int INVALID_VERIFYCODE = 1;
	private static final int PHONE_ALREADY_EXIST = 20;

	private InvitationMethod() {
	}

	public static InvitationMethod getMethod() {
		return new InvitationMethod();
	}

	public MethodResult getVerifyCode() throws Exception {
		HttpResult result = new HttpResult();
		MethodResult methodResult = new MethodResult(EventType.GET_AUTH_CODE_SUCCESS);
		String command = createCommand();
		Log.d("DDD getVerifyCode", " str : " + command);
		result = HttpClientHelper.executeGet(command);

		Log.d("DJC", "getVerifyCode result:" + result);
		if (result.getResCode() != HttpStatus.SC_OK) {
			methodResult.setResultType(EventType.INVITE_FAIL);
		}
		return methodResult;
	}

	private String createCommand() {
		String url = String.format(ServerUrls.GET_AUTH_CODE_URL, DataUtils.getAccount());
		return url;
	}

	public MethodResult invite(String phone, String name, String relation, String verifycode) throws Exception {
		MethodResult methodResult = new MethodResult(EventType.INVITE_SUCCESS);
		HttpResult result = new HttpResult();
		String url = createUrl();

		String content = createContent(phone, name, relation, verifycode);

		Log.d("DJC", "invite cmd:" + url + " content=" + content);
		result = HttpClientHelper.executePost(url, content);

		Log.d("DJC", "invite result:" + result);
		if (result.getResCode() != HttpStatus.SC_OK) {
			int errorCode = result.getErrorCode();
			switch (errorCode) {
			case INVALID_VERIFYCODE:
				methodResult.setResultType(EventType.AUTH_CODE_IS_INVALID);
				break;
			case PHONE_ALREADY_EXIST:
				methodResult.setResultType(EventType.INVITE_PHONE_ALREADY_EXIST);
				break;
			default:
				methodResult.setResultType(EventType.INVITE_FAIL);
				break;
			}
		}

		return methodResult;
	}

	private String createContent(String phone, String name, String relationship, String verifyCode)
			throws JSONException {
		JSONObject obj = new JSONObject();

		ParentInfo parentInfo = DataMgr.getInstance().getSelfInfoByPhone();

		JSONObject from = new JSONObject();

		from.put("parent_id", parentInfo.getParent_id());
		from.put("school_id", Integer.parseInt(DataMgr.getInstance().getSchoolID()));
		from.put("name", parentInfo.getName());
		from.put("phone", parentInfo.getPhone());
		from.put("portrait", parentInfo.getPortrait());
		from.put("gender", 0);
		from.put("birthday", "");
		from.put("timestamp", parentInfo.getTimestamp());
		from.put("member_status", parentInfo.getMember_status());
		from.put("status", 1);
		from.put("company", "");
		from.put("created_at", 0);
		from.put("id", 0);

		obj.put("from", from);

		JSONObject to = new JSONObject();

		to.put("phone", phone);
		to.put("name", name);
		to.put("relationship", relationship);

		obj.put("to", to);

		JSONObject code = new JSONObject();

		code.put("phone", phone);
		code.put("code", verifyCode);

		obj.put("code", code);

		return obj.toString();
	}

	private String createUrl() {
		String url = String.format(ServerUrls.INVITE_URL, DataMgr.getInstance().getSchoolID());
		return url;
	}

}
