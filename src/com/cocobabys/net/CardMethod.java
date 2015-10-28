package com.cocobabys.net;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.cocobabys.bean.RelationInfo;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.utils.DataUtils;

public class CardMethod {
	// 重复绑定
	private static final int DUPLICATED_RELATIONSHIP = 3;

	// 卡号被其他家长绑定
	private static final int CARD_ALREADY_USED = 6;

	private static final int CARD_INVALID = 4;

	private static final int NO_ERROR = -1;

	private CardMethod() {
	}

	public static CardMethod getMethod() {
		return new CardMethod();
	}

	private String createCommand(String cardnum) {
		String url = String.format(ServerUrls.BIND_CARD_URL, DataMgr.getInstance().getSchoolID(), cardnum);
		return url;
	}

	public MethodResult bindCard(String cardnum) throws Exception {
		HttpResult result = new HttpResult();
		MethodResult methodResult = new MethodResult(EventType.BIND_CARD_FAIL);
		String command = createCommand(cardnum);

		String content = createContent();

		Log.d("DJC", "bindCard cmd:" + command + " content=" + content);
		result = HttpClientHelper.executePost(command, content);

		Log.d("DJC", "bindCard result:" + result);
		int errorCode = result.getErrorCode();

		switch (errorCode) {
		case NO_ERROR:
			if (result.getResCode() == HttpStatus.SC_OK) {
				methodResult.setResultType(EventType.BIND_CARD_SUCCESS);

				String card = result.getJsonObject().optString("card");
				String id = result.getJsonObject().optString("id");
				DataUtils.saveRelationInfo(DataMgr.getInstance().getSelectedChild().getServer_id(), id, card);
				// DataUtils.saveProp(card, id);
			}
			break;
		case DUPLICATED_RELATIONSHIP:
		case CARD_ALREADY_USED:
			methodResult.setResultType(EventType.BIND_DUPLICATED);
			break;
		case CARD_INVALID:
			methodResult.setResultType(EventType.BIND_CARD_INVALID);
			break;
		default:
			methodResult.setResultType(EventType.BIND_CARD_FAIL);
			break;
		}

		return methodResult;
	}

	private String createContent() throws JSONException {
		String childid = DataMgr.getInstance().getSelectedChild().getServer_id();

		JSONObject obj = new JSONObject();
		ParentInfo parentInfo = DataMgr.getInstance().getSelfInfoByPhone();

		obj.put("relationship", parentInfo.getRelationship());

		RelationInfo relationInfo = DataUtils.getRelationInfo(childid);

		obj.put("id", Integer.valueOf(relationInfo.getRelationid()));

		JSONObject parent = new JSONObject();
		parent.put("phone", parentInfo.getPhone());

		obj.put("parent", parent);

		JSONObject child = new JSONObject();

		child.put("child_id", childid);

		obj.put("child", child);

		return obj.toString();
	}

}
