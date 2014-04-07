package com.cocobabys.dbmgr.info;

public class Notice {
	public static final String ID = "_id";
	public static final String TITLE = "title";
	public static final String CONTENT = "content";
	public static final String TIMESTAMP = "timestamp";
	public static final String PUBLISHER = "publisher";
	public static final String NOTICE_TYPE = "type";
	public static final String READ = "read";
	// 冗余字段，只有刷卡消息需要用到，表示是哪位小孩刷卡,这里暂时这样实现
	public static final String CHILD_ID = "child_id";

	private String title = "";
	private String content = "";
	private String timestamp = "";
	private String publisher = "";
	private String child_id = "";

	public String getChild_id() {
		return child_id;
	}

	public void setChild_id(String child_id) {
		this.child_id = child_id;
	}

	private int type = 0;
	private String custom_content = "";
	private long id = 0;
	// 0表示通知未读,1表示已读
	private int read = 0;

	// 点击通知栏清除所有通知时，是否清除本通知，true清除，false不清除
	private boolean clear = true;

	public boolean isClear() {
		return clear;
	}

	public void setClear(boolean clear) {
		this.clear = clear;
	}

	public int getRead() {
		return read;
	}

	public void setRead(int read) {
		this.read = read;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	// 以下2个字段不存入数据库，只是在通知栏上需要显示
	private String ticker = "";
	private Class<?> toClass;

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

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getCustom_content() {
		return custom_content;
	}

	public void setCustom_content(String custom_content) {
		this.custom_content = custom_content;
	}

	public Class<?> getToClass() {
		return toClass;
	}

	public void setToClass(Class<?> toClass) {
		this.toClass = toClass;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	@Override
	public String toString() {
		return "Notice [title=" + title + ", content=" + content
				+ ", timestamp=" + timestamp + ", publisher=" + publisher
				+ ", child_id=" + child_id + ", type=" + type
				+ ", custom_content=" + custom_content + ", id=" + id
				+ ", read=" + read + ", clear=" + clear + ", ticker=" + ticker
				+ ", toClass=" + toClass + "]";
	}

}
