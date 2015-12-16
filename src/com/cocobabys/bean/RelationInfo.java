package com.cocobabys.bean;

import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.RelationshipInfo;

public class RelationInfo {
	private String relationship = "";
	private String childid = "";
	private String relationid = "";
	private String cardnum = "";

	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public String getChildid() {
		return childid;
	}

	public void setChildid(String childid) {
		this.childid = childid;
	}

	public String getRelationid() {
		return relationid;
	}

	public void setRelationid(String relationid) {
		this.relationid = relationid;
	}

	public String getCardnum() {
		return cardnum;
	}

	public void setCardnum(String cardnum) {
		this.cardnum = cardnum;
	}

	public RelationshipInfo toRelationshipInfo() {
		RelationshipInfo info = new RelationshipInfo();
		info.setChild_id(childid);
		info.setRelationship(relationship);
		info.setParent_id(DataMgr.getInstance().getSelfInfoByPhone().getParent_id());
		return info;
	}
}
