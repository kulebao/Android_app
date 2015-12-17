package com.cocobabys.net;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.utils.DataUtils;

import android.util.Log;

public class InvitationMethod {
	private static final int INVALID_VERIFYCODE = 1;
	private static final int PHONE_ALREADY_EXIST = 20;

	private static final int ERR_REQUEST_TOO_OFTEN = 1;
	private static final int ERR_INVITEE_PHONE_INVALID = 4;
	// 被邀请的手机号码已经在幼儿宝注册
	private static final int ERR_INVITEE_PHONE_ALREADY_EXIST = 8;

	private InvitationMethod() {
	}

	public static InvitationMethod getMethod() {
		return new InvitationMethod();
	}

	public MethodResult getVerifyCode(String inviteePhone) throws Exception {
		HttpResult result = new HttpResult();
		MethodResult methodResult = new MethodResult(EventType.GET_AUTH_CODE_SUCCESS);
		String command = createPostCodeCommand(inviteePhone);
		String body = createPostBody(inviteePhone);

		Log.d("DDD getVerifyCode", " str : " + command);
		Log.d("DDD getVerifyCode", " body : " + body);
		result = HttpClientHelper.executePost(command, body);

		Log.d("DJC", "getVerifyCode result:" + result);

		if (result.getResCode() == HttpStatus.SC_OK) {
			int errorCode = result.getErrorCode();
			// 兼容老接口，老接口无论对错都会返回200，通过errorcode判断错误类型，新接口错误会返回500，通过errorcode判断错误类型
			if (errorCode == ERR_INVITEE_PHONE_INVALID) {
				methodResult.setResultType(EventType.INVITE_PHONE_INVALID);
			}
		} else {
			int errorCode = result.getErrorCode();
			if (ERR_REQUEST_TOO_OFTEN == errorCode) {
				methodResult.setResultType(EventType.GET_AUTH_CODE_TOO_OFTEN);
			} else if (ERR_INVITEE_PHONE_ALREADY_EXIST == errorCode) {
				methodResult.setResultType(EventType.GET_INVITED_CODE_FAIL_PHONE_ALREADY_EXIST);
			} else {
				methodResult.setResultType(EventType.GET_AUTH_CODE_FAIL);
			}
		}

		return methodResult;
	}

	private String createPostCodeCommand(String inviteePhone) {
		String url = String.format(ServerUrls.INVITE_CODE_URL, inviteePhone);
		return url;
	}

	private String createPostBody(String inviteePhone) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("host", DataUtils.getAccount());
		obj.put("invitee", inviteePhone);
		return obj.toString();
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
