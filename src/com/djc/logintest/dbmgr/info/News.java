package com.djc.logintest.dbmgr.info;

import com.djc.logintest.utils.Utils;

public class News {
	public static final String ID = "_id";
	public static final String TITLE = "title";
	public static final String CONTENT = "content";
	public static final String TIMESTAMP = "timestamp";
	public static final String PUBLISHER = "publisher";
	public static final String NEWS_TYPE = "news_type";
	public static final String NEWS_SERVER_ID = "news_server_id";

	private String title = "";
	private String content = "";
	private long timestamp = 0;
	private String publisher = "";

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
	
	public String getFormattedTime(){
		String ret = "";
		try {
			ret = Utils.convertTime(timestamp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

}
