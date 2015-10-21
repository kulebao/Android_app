package com.cocobabys.bean;

public class SettingInfo {
	private int nameid = 0;
	private int picid = 0;

	public SettingInfo(int nameid, int picid) {
		this.nameid = nameid;
		this.picid = picid;
	}

	public int getNameid() {
		return nameid;
	}

	public void setNameid(int nameid) {
		this.nameid = nameid;
	}

	public int getPicid() {
		return picid;
	}

	public void setPicid(int picid) {
		this.picid = picid;
	}

}
