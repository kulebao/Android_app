package com.cocobabys.dbmgr.info;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cocobabys.dbmgr.DataMgr;

public class ChildInfo {
	public static final int STATUS_SELECTED = 1;
	public static final int STATUS_UNSELECTED = 0;

	public static final String ID = "_id";
	public static final String CHILD_NICK_NAME = "child_nick_name";
	public static final String CHILD_LOCAL_HEAD_ICON = "local_url";
	public static final String CHILD_SERVER_HEAD_ICON = "server_url";
	public static final String CHILD_BIRTHDAY = "child_birthday";
	// 有多位小孩的情况下，标示当前被选中的小孩
	public static final String SELECTED = "selected";
	// 服务器上小孩id，restful定位小孩资源
	public static final String SERVER_ID = "server_id";
	public static final String CLASS_ID = "class_id";
	public static final String CLASS_NAME = "class_name";
	public static final String CHILD_NAME = "child_name";
	public static final String GENDER = "gender";

	private String child_nick_name = "";
	private String local_url = "";
	private String server_url = "";
	private long child_birthday = 0;
	// 0表示未选中， 1表示选中，只能有一条记录为1
	private int selected = STATUS_UNSELECTED;
	private long timestamp = 0;
	private String server_id = "";
	private String class_id = "";
	private String class_name = "";
	private String child_name = "";
	private int gender = 0;

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getChild_name() {
		return child_name;
	}

	public void setChild_name(String child_name) {
		this.child_name = child_name;
	}

	public String getClass_name() {
		return class_name;
	}

	public void setClass_name(String class_name) {
		this.class_name = class_name;
	}

	public String getClass_id() {
		return class_id;
	}

	public void setClass_id(String class_id) {
		this.class_id = class_id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getServer_id() {
		return server_id;
	}

	public void setServer_id(String server_id) {
		this.server_id = server_id;
	}

	public int getSelected() {
		return selected;
	}

	public void setSelected(int selected) {
		this.selected = selected;
	}

	private int id = 0;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getChild_nick_name() {
		return child_nick_name;
	}

	public void setChild_nick_name(String child_nick_name) {
		this.child_nick_name = child_nick_name;
	}

	@Deprecated
	// 后续通过NativeMediumMgr来获取本地小孩头像路径
	public String getLocal_url() {
		NativeMediumInfo nativeMediumInfo = DataMgr.getInstance().getNativeMediumInfo(server_url);
		return nativeMediumInfo == null ? "" : nativeMediumInfo.getValue();
		// return local_url;
	}

	@Deprecated
	// 后续通过NativeMediumMgr来获取本地小孩头像路径
	public void setLocal_url(String local_url) {
		this.local_url = local_url;
	}

	public String getServer_url() {
		return server_url;
	}

	public void setServer_url(String server_url) {
		this.server_url = server_url;
	}

	public long getChild_birthday() {
		return child_birthday;
	}

	public void setChild_birthday(long child_birthday) {
		this.child_birthday = child_birthday;
	}

	public static List<ChildInfo> jsonArrayToList(JSONArray array) {
		List<ChildInfo> list = new ArrayList<ChildInfo>();
		for (int i = 0; i < array.length(); i++) {
			try {
				JSONObject obj = array.getJSONObject(i);
				ChildInfo info = jsonObjToChildInfo(obj);
				// 简单的把第一个小孩设置为选中
				if (i == 0) {
					info.setSelected(STATUS_SELECTED);
				}

				if (!list.contains(info)) {
					list.add(info);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	public static ChildInfo jsonObjToChildInfo(JSONObject obj) throws JSONException {
		ChildInfo info = new ChildInfo();
		info.setServer_id(obj.getString("child_id"));
		info.setChild_nick_name(obj.getString("nick"));
		info.setServer_url(obj.getString("portrait"));
		try {
			info.setChild_birthday(InfoHelper.getYearMonthDayFormat().parse(obj.getString("birthday")).getTime());
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		info.setTimestamp(obj.getLong("timestamp"));
		info.setChild_name(obj.getString("name"));
		try {
			info.setClass_id(obj.getString("class_id"));
			info.setClass_name(obj.getString("class_name"));
			info.setGender(obj.getInt("gender"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}

	@Override
	public String toString() {
		return "ChildInfo [child_nick_name=" + child_nick_name + ", server_id=" + server_id + ", class_id=" + class_id
				+ ", child_name=" + child_name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((server_id == null) ? 0 : server_id.hashCode());
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
		ChildInfo other = (ChildInfo) obj;
		if (server_id == null) {
			if (other.server_id != null)
				return false;
		} else if (!server_id.equals(other.server_id))
			return false;
		return true;
	}

}
