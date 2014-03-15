package com.djc.logintest.dbmgr.info;

import java.io.File;

import android.util.Log;

import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.utils.Utils;

public class News {
	public static final String ID = "_id";
	public static final String TITLE = "title";
	public static final String CONTENT = "content";
	public static final String TIMESTAMP = "timestamp";
	public static final String PUBLISHER = "publisher";
	public static final String NEWS_TYPE = "news_type";
	public static final String NEWS_SERVER_ID = "news_server_id";
	public static final String ICON_URL = "icon_url";
	public static final String CLASS_ID = "class_id";
	private static final String NEWS_ICON = "news_icon";

	private String title = "";
	private String content = "";
	private long timestamp = 0;
	private String publisher = "";
	private String icon_url = "";
	private int class_id = 0;

	private int type = 0;
	private int id = 0;
	private int news_server_id = 0;

	public int getNews_server_id() {
		return news_server_id;
	}

	public void setNews_server_id(int news_server_id) {
		this.news_server_id = news_server_id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getIcon_url() {
		return icon_url;
	}

	public void setIcon_url(String icon_url) {
		this.icon_url = icon_url;
	}

	public int getClass_id() {
		return class_id;
	}

	public void setClass_id(int class_id) {
		this.class_id = class_id;
	}

	// 返回消息来源，如果classid为0，则来自学校，否则来自具体班级
	public String getFrom() {
		String from = DataMgr.getInstance().getSchoolInfo().getSchool_name();
		if (class_id != 0) {
			String className = DataMgr.getInstance().getClassNameByClassID(
					class_id);
			Log.d("EEE", "getFrom className=" + className + "  class_id="
					+ class_id);
			from += className;
		}

		return from;
	}

	// 返回公告本地图片保存路径，如果不存在则返回null
	public String getNewsLocalIconPath() {
		String dir = Utils.getSDCardPicRootPath() + File.separator + NEWS_ICON
				+ File.separator;
		Utils.mkDirs(dir);
		String url = dir + news_server_id;
		return url;
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

}
