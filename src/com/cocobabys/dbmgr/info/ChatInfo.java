package com.cocobabys.dbmgr.info;

import java.io.File;

import android.text.TextUtils;
import android.util.Log;

import com.cocobabys.R;
import com.cocobabys.upload.UploadFactory;
import com.cocobabys.utils.Utils;

public class ChatInfo {
	public static final String ID = "_id";
	public static final String SENDER = "sender";
	public static final String CONTENT = "content";
	public static final String TIMESTAMP = "timestamp";
	public static final String SERVER_ID = "server_id";
	public static final String ICON_URL = "icon_url";
	public static final String SEND_RESULT = "send_result";
	// 教师的手机号
	public static final String PHONE = "phone";

	public static final int SEND_SUCCESS = 0;
	public static final int SEND_FAIL = 1;

	private String sender = "";
	private String content = "";
	private long timestamp = 0;
	private String icon_url = "";
	private String phone = "";

	private int id = 0;
	private int server_id = 0;
	// 0表示发送成功，1表示失败
	private int send_result = 0;

	public int getSend_result() {
		return send_result;
	}

	public void setSend_result(int send_result) {
		this.send_result = send_result;
	}

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

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
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

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
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

	public int getLayoutID() {
		int layoutid = 0;
		if (TextUtils.isEmpty(getSender())) {
			layoutid = R.layout.chat_item_right;
		} else {
			layoutid = R.layout.chat_item_left;
		}

		return layoutid;
	}

	public boolean isSendBySelf() {
		return TextUtils.isEmpty(getSender());
	}

	public String getLocalUrl() {
		String localUrl = "";
		if (!"".equals(icon_url)) {
			if ("".equals(getSender())) {
				localUrl = getIcon_url().replace(
						UploadFactory.getUploadHost(),
						Utils.getSDCardPicRootPath() + File.separator);
			} else {
				// 老师发的图片，不一定是来自阿里oss,可能是七牛，后续要统一
				// 保存到本地时，以服务器端时间搓作为文件名
				localUrl = Utils.getSDCardPicRootPath() + File.separator
						+ Utils.getChatIconUrl(getTimestamp());
			}
		}
		Log.d("DDD", "getLocalUrl =" + localUrl);
		return localUrl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + server_id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChatInfo other = (ChatInfo) obj;
		if (server_id != other.server_id)
			return false;
		return true;
	}

}
