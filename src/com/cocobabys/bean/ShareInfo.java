package com.cocobabys.bean;

import com.cocobabys.constant.JSONConstant;

public class ShareInfo {
	// 分享的文字内容
	private String content = "";

	// 本地源文件路径
	private String localUrl = "";
	// 分享出去的路径
	private String mediaUrl = "";
	// 分享的类型
	private String mediaType = JSONConstant.IMAGE_TYPE;

	public ShareInfo(String content) {
		this.content = content;
	}

	public ShareInfo(String content, String localUrl) {
		this.content = content;
		this.localUrl = localUrl;
	}

	public ShareInfo(String content, String localUrl, String mediaType) {
		this.content = content;
		this.localUrl = localUrl;
		this.mediaType = mediaType;
	}

	public ShareInfo() {
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getLocalUrl() {
		return localUrl;
	}

	public void setLocalUrl(String localUrl) {
		this.localUrl = localUrl;
	}

	public String getMediaUrl() {
		return mediaUrl;
	}

	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

	public String getMediaType() {
		return mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public boolean isValidShareType() {
		return JSONConstant.IMAGE_TYPE.equals(mediaType)
				|| JSONConstant.VIDEO_TYPE.equals(mediaType);
	}
}
