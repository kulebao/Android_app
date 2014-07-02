package com.cocobabys.dbmgr.info;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.cocobabys.constant.JSONConstant;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.utils.Utils;

public class InfoHelper {
	private static final String BABY_ICON_NAME = "baby_icon";
	private static final String SCHOOL_LOGO = "school_logo";
	public static final String TIMESTAMP = "timestamp";

	// public static final SimpleDateFormat YEAR_MONTH_DAY_FORMAT = new
	// SimpleDateFormat("yyyy-MM-dd");
	// public static final SimpleDateFormat HOUR_MINUTE_FORMAT = new
	// SimpleDateFormat("HH:mm");

	public static SimpleDateFormat getYearMonthDayFormat() {
		return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
	}

	public static SimpleDateFormat getHourMinuteFormat() {
		return new SimpleDateFormat("HH:mm", Locale.CHINESE);
	}

	public static String getChildrenLocalIconPath(String childid) {
		String dir = Utils.getSDCardPicRootPath() + File.separator + BABY_ICON_NAME + File.separator;

		Utils.mkDirs(dir);
		String url = dir + childid;
		Log.d("DDD", "getChildrenDefaultLocalIconPath url=" + url);
		return url;
	}

	public static String getDefaultSchoolLocalIconPath() {
		String url = Utils.getSDCardPicRootPath() + File.separator + SCHOOL_LOGO;
		Log.d("DDD", "getChildrenDefaultLocalIconPath url=" + url);
		return url;
	}

	// 不要修改，与服务器约定好的
	public static JSONObject childInfoToJSONObject(ChildInfo info) {
		JSONObject object = new JSONObject();
		try {
			object.put("name", info.getChild_name());
			object.put(NICK, info.getChild_nick_name());
			object.put(BIRTHDAY, InfoHelper.getYearMonthDayFormat().format(new Date(info.getChild_birthday())));
			object.put("gender", info.getGender());
			object.put(PHOTO, info.getServer_url());
			object.put("class_id", Integer.valueOf(info.getClass_id()));
			object.put("child_id", info.getServer_id());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return object;
	}

	public static String formatChatContent(String content, String imageUrl, String childid, String mediaType) {
		JSONObject jsonObject = new JSONObject();
		JSONObject mediaJsonObj = new JSONObject();
		JSONObject senderJsonObj = new JSONObject();
		try {
			jsonObject.put(JSONConstant.TOPIC, childid);
			jsonObject.put(NewChatInfo.CONTENT, content);

			mediaJsonObj.put("url", imageUrl);
			mediaJsonObj.put("type", mediaType);
			jsonObject.put(JSONConstant.MEDIA, mediaJsonObj);

			senderJsonObj.put("id", DataMgr.getInstance().getSelfInfoByPhone().getParent_id());
			senderJsonObj.put("type", NewChatInfo.PARENT_TYPE);
			jsonObject.put(JSONConstant.SENDER, senderJsonObj);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

	public static final String WEEK_DETAIL = "week";
	public static final String MON = "mon";
	public static final String TUE = "tue";
	public static final String WED = "wed";
	public static final String THU = "thu";
	public static final String FRI = "fri";
	public static final String TIME = "time";
	public static final String AM = "am";
	public static final String PM = "pm";

	public static String BIRTHDAY = "birthday";
	public static String PHOTO = "portrait";
	public static String NICK = "nick";

	// 学校信息最近更新时间
}
