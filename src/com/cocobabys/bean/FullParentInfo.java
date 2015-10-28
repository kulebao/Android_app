package com.cocobabys.bean;

import com.cocobabys.dbmgr.info.ParentInfo;

public class FullParentInfo extends ParentInfo {
	private int gender = 0;
	private int school_id = 0;
	private String birthday = "";
	private String company = "";

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public int getSchool_id() {
		return school_id;
	}

	public void setSchool_id(int school_id) {
		this.school_id = school_id;
	}

}
