package com.cocobabys.dbmgr.info;

public class IMGroupInfo {
	public static final String CLASS_ID = "class_id";
	public static final String GROUP_ID = "group_id";
	public static final String GROUP_NAME = "group_name";
	public static final String ID = "_id";

	private int id = 0;
	private int class_id = 0;
	private String group_id = "";
	private String group_name = "";

	public IMGroupInfo() {
	}

	public IMGroupInfo(int class_id, String group_id, String group_name) {
		this.class_id = class_id;
		this.group_id = group_id;
		this.group_name = group_name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getClass_id() {
		return class_id;
	}

	public void setClass_id(int class_id) {
		this.class_id = class_id;
	}

	public String getGroup_id() {
		return group_id;
	}

	public void setGroup_id(String group_id) {
		this.group_id = group_id;
	}

	public String getGroup_name() {
		return group_name;
	}

	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}

	@Override
	public String toString() {
		return "IMGroupInfo [class_id=" + class_id + ", group_id=" + group_id + ", group_name=" + group_name + "]";
	}

}
