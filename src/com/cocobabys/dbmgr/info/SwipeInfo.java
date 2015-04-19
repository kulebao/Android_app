package com.cocobabys.dbmgr.info;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.cocobabys.constant.JSONConstant;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

public class SwipeInfo {
	private static String SWIPE_ICON = "swipe_icon";
	private static String SWIPE_ICON_MINI = "swipe_icon_mini";
	private static final String SWIPE_CARD_TITLE = "尊敬的用户 %s 您好:";
	private static final String SWIPE_CARD_IN_BODY = "您的小孩 %s 已于%s 由 %s 刷卡入园!";
	private static final String SWIPE_CARD_OUT_BODY = "您的小孩 %s 已于%s 由 %s 刷卡离园!";

	private static final String SWIPE_CARD_GET_ON = "您的小孩 %s 已于%s 坐上校车!";
	private static final String SWIPE_CARD_GET_OFF = "您的小孩 %s 已于%s 离开校车!";

	public static final String ID = "_id";
	public static final String TIMESTAMP = "timestamp";
	public static final String TYPE = "type";
	public static final String CHILD_ID = "child_id";
	public static final String ICON_URL = "icon_url";
	public static final String PARENT_NAME = "parent_name";
	// 暂不记录入数据库
	public static final String AD = "ad";

	private long timestamp = 0;

	private int type = 0;
	private int id = 0;
	private String child_id = "";
	private String url = "";
	private String parent_name = "";

	// 广告词
	private String ad = "";

	public String getAd() {
		return ad;
	}

	public void setAd(String ad) {
		this.ad = ad;
	}

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
		StringBuffer buffer = new StringBuffer();
		if (!ad.isEmpty()) {
			buffer.append(Utils.getAdNotice(ad));
		}

		switch (type) {
		case JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKIN:
			buffer.append(String.format(SWIPE_CARD_IN_BODY, nickname,
					getFormattedTime(), parent_name));
			break;
		case JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKOUT:
			buffer.append(String.format(SWIPE_CARD_OUT_BODY, nickname,
					getFormattedTime(), parent_name));
			break;
		case JSONConstant.NOTICE_TYPE_AFTERNOON_GET_ON:
		case JSONConstant.NOTICE_TYPE_MORNING_GET_ON:
			buffer.append(String.format(SWIPE_CARD_GET_ON, nickname,
					getFormattedTime()));
			break;
		case JSONConstant.NOTICE_TYPE_AFTERNOON_GET_OFF:
		case JSONConstant.NOTICE_TYPE_MORNING_GET_OFF:
			buffer.append(String.format(SWIPE_CARD_GET_OFF, nickname,
					getFormattedTime()));
			break;
		default:
			break;
		}
		return buffer.toString();
	}

	public String getNoticeTitle() {
		String title = String.format(SWIPE_CARD_TITLE,
				DataUtils.getProp(JSONConstant.USERNAME));
		return title;
	}

	public static SwipeInfo toSwipeInfo(JSONObject obj) throws JSONException {
		int type = obj.getInt(JSONConstant.NOTIFICATION_TYPE);
		final String record_url = obj.getString("record_url");
		String child_id = obj.getString("child_id");
		String parent_name = obj.getString("parent_name");
		String ad = getAd(obj);
		long timestamp = Long.valueOf(obj.getString(JSONConstant.TIME_STAMP));
		SwipeInfo info = new SwipeInfo();
		info.setChild_id(child_id);
		info.setType(type);
		info.setUrl(record_url);
		info.setTimestamp(timestamp);
		info.setParent_name(parent_name);
		info.setAd(ad);
		return info;
	}

	private static String getAd(JSONObject obj) {
		String ad = "";
		try {
			// ad 是后面增加的字段，在获取刷卡历史记录时，旧记录没有这个字段，所以需要特殊处理
			ad = obj.getString("ad");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ad;
	}

	// 返回亲子作业本地图片保存路径，如果不存在则返回""
	public String getSwipeLocalIconPath() {
		return getIconImpl(false);
	}

	// 返回亲子作业本地 缩略图片保存路径
	public String getSwipeLocalMiniIconPath() {
		return getIconImpl(true);
	}

	private String getIconImpl(boolean isMini) {
		// 服务器上不存在图片，本地一定不存在
		if (TextUtils.isEmpty(url)) {
			return "";
		}

		String path = isMini ? SWIPE_ICON_MINI : SWIPE_ICON;

		String dir = Utils.getSDCardPicRootPath() + File.separator + path
				+ File.separator;
		Utils.mkDirs(dir);
		String url = dir + timestamp;
		return url;
	}

}
