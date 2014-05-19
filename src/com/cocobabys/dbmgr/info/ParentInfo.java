package com.cocobabys.dbmgr.info;

import org.json.JSONException;
import org.json.JSONObject;

public class ParentInfo {
	public static final String ID = "_id";
	// 服务器端的id
	public static String PARENT_ID = "parent_id";
	public static String PARENT_NAME = "name";
	public static String PHONE = "phone";
	public static String PORTRAIT = "portrait";
	public static String MEMBER_STATUS = "member_status";
	public static String CARD_NUMBER = "card";
	public static String RELATIONSHIP = "relationship";
	public static String TIMESTAMP = "timestamp";

	private String parent_id = "";
	private String name = "";
	private String phone = "";
	private String portrait = "";
	private String card = "";
	private String relationship = "";
	private int member_status = -1;
	private long timestamp = -1;

	private int id = 0;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getParent_id() {
		return parent_id;
	}

	public void setParent_id(String parent_id) {
		this.parent_id = parent_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCard() {
		return card;
	}

	public void setCard(String card) {
		this.card = card;
	}

	public int getMember_status() {
		return member_status;
	}

	public void setMember_status(int member_status) {
		this.member_status = member_status;
	}

	public String getPortrait() {
		return portrait;
	}

	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}

	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public static ParentInfo parse(JSONObject jsonObject) throws JSONException {
		ParentInfo info = new ParentInfo();

		JSONObject parentJson = jsonObject.getJSONObject("parent");

		info.setCard(jsonObject.getString(CARD_NUMBER));
		info.setMember_status(parentJson.getInt(MEMBER_STATUS));
		info.setParent_id(parentJson.getString(PARENT_ID));
		info.setName(parentJson.getString(PARENT_NAME));
		info.setPhone(parentJson.getString(PHONE));
		info.setPortrait(parentJson.getString(PORTRAIT));
		info.setTimestamp(parentJson.getLong(TIMESTAMP));
		info.setRelationship(jsonObject.getString(RELATIONSHIP));
		return info;
	}

}
