package com.djc.logintest.dbmgr.info;

import java.io.File;

import android.text.TextUtils;

import com.djc.logintest.utils.Utils;

public class Homework {
	public static final String ID = "_id";
	public static final String TITLE = "title";
	public static final String CONTENT = "content";
	public static final String TIMESTAMP = "timestamp";
	public static final String PUBLISHER = "publisher";
	public static final String SERVER_ID = "server_id";
	public static final String ICON_URL = "icon_url";
	private static final String HOMEWORK_ICON = "homework_icon";
	public static final String CLASS_ID = "class_id";

	private String title = "";
	private String content = "";
	private long timestamp = 0;
	private String publisher = "";
	private String icon_url = "";
	private int class_id = 0;

	public int getClass_id() {
		return class_id;
	}

	public void setClass_id(int class_id) {
		this.class_id = class_id;
	}

	private int id = 0;
	private int server_id = 0;

	public String getIcon_url() {
		return icon_url;
	}

	public void setIcon_url(String icon_url) {
		this.icon_url = icon_url;
	}

	public int getServer_id() {
		return server_id;
	}

	public void setServer_id(int server_id) {
		this.server_id = server_id;
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

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
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

	// 返回亲子作业本地图片保存路径，如果不存在则返回null
	public String getHomeWorkLocalIconPath() {
		// 服务器上不存在图片，本地一定不存在
		if (TextUtils.isEmpty(icon_url)) {
			return "";
		}
		String dir = Utils.getSDCardPicRootPath() + File.separator
				+ HOMEWORK_ICON + File.separator;
		Utils.mkDirs(dir);
		String url = dir + server_id;
		return url;
	}

}
