package com.cocobabys.dbmgr.info;

import org.json.JSONException;
import org.json.JSONObject;

import com.cocobabys.constant.JSONConstant;
import com.cocobabys.utils.Utils;

public class SwipeInfo {
	private static final String SWIPE_CARD_TITLE = "尊敬的用户 %s 您好:";

	private static final String SWIPE_CARD_IN_BODY = "您的小孩 %s 已于%s 由 %s 刷卡入园!";
	private static final String SWIPE_CARD_OUT_BODY = "您的小孩 %s 已于%s 由 %s 刷卡离园!";

	public static final String ID = "_id";
	public static final String TIMESTAMP = "timestamp";
	public static final String TYPE = "type";
	public static final String CHILD_ID = "child_id";
	public static final String ICON_URL = "icon_url";
	public static final String PARENT_NAME = "parent_name";

	private long timestamp = 0;

	private int type = 0;
	private int id = 0;
	private String child_id = "";
	private String url = "";
	private String parent_name = "";

	public String getParent_name() {
		return parent_name;
	}

	public void setParent_name(String parent_name) {
		this.parent_name = parent_name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getChild_id() {
		return child_id;
	}

	public void setChild_id(String child_id) {
		this.child_id = child_id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getFormattedTime() {
		String ret = "";
		try {
			ret = Utils.convertTime(timestamp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public String getNoticeBody(String nickname) {
		String sample = (type == JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKIN ? SWIPE_CARD_IN_BODY : SWIPE_CARD_OUT_BODY);

		String body = String.format(sample, nickname, getFormattedTime(), parent_name);
		return body;
	}

	public String getNoticeTitle() {
		String title = String.format(SWIPE_CARD_TITLE, Utils.getProp(JSONConstant.USERNAME));
		return title;
	}

	public static SwipeInfo toSwipeInfo(JSONObject obj) throws JSONException {
		int type = obj.getInt(JSONConstant.NOTIFICATION_TYPE);
		final String record_url = obj.getString("record_url");
		String child_id = obj.getString("child_id");
		String parent_name = obj.getString("parent_name");
		long timestamp = Long.valueOf(obj.getString(JSONConstant.TIME_STAMP));
		SwipeInfo info = new SwipeInfo();
		info.setChild_id(child_id);
		info.setType(type);
		info.setUrl(record_url);
		info.setTimestamp(timestamp);
		info.setParent_name(parent_name);
		return info;
	}

}