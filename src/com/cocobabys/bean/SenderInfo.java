package com.cocobabys.bean;

public class SenderInfo {
	public static final String TEACHER_TYPE = "t";
	public static final String PARENT_TYPE = "p";

	private String senderID = "";
	private String senderType = "";

	public String getSenderID() {
		return senderID;
	}

	public void setSenderID(String senderID) {
		this.senderID = senderID;
	}

	public String getSenderType() {
		return senderType;
	}

	public void setSenderType(String senderType) {
		this.senderType = senderType;
	}

}
