package com.cocobabys.dbmgr.info;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.taskmgr.GetTeacherTask;
import com.cocobabys.utils.Utils;

public class Teacher {
	public static final String ID = "_id";
	public static final String NAME = "name";
	public static final String HEAD_ICON = "head_icon";
	public static final String TIMESTAMP = "timestamp";
	public static final String BIRTHDAY = "birthday";
	// 服务器上小孩id，restful定位小孩资源
	public static final String SERVER_ID = "server_id";
	public static final String WORKGROUP = "workgroup";
	public static final String WORKDUTY = "workduty";
	public static final String GENDER = "gender";
	public static final String SHOOL_ID = "shool_id";
	public static final String PHONE = "phone";
	private static final String TEACHER_ICON = "teacher_icon";
	private int id = 0;
	private String name = "";
	private String head_icon = "";
	private String birthday = "2000-01-01";
	private long timestamp = 0;
	private String server_id = "";
	private String workgroup = "";
	private String workduty = "";
	private String phone = "";
	private int shool_id = 0;
	private int gender = 0;

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHead_icon() {
		return head_icon;
	}

	public void setHead_icon(String head_icon) {
		this.head_icon = head_icon;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
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

	public String getWorkgroup() {
		return workgroup;
	}

	public void setWorkgroup(String workgroup) {
		this.workgroup = workgroup;
	}

	public String getWorkduty() {
		return workduty;
	}

	public void setWorkduty(String workduty) {
		this.workduty = workduty;
	}

	public int getShool_id() {
		return shool_id;
	}

	public void setShool_id(int shool_id) {
		this.shool_id = shool_id;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	// 返回教师头像本地图片保存路径，如果不存在则返回null
	public String getLocalIconPath() {
		// 服务器上不存在图片，本地一定不存在
		if (TextUtils.isEmpty(head_icon)) {
			return "";
		}
		String dir = Utils.getSDCardPicRootPath() + File.separator
				+ TEACHER_ICON + File.separator;
		Utils.mkDirs(dir);
		String url = dir + phone;
		return url;
	}

	public static String getLocalIconPath(String phone) {
		String dir = Utils.getSDCardPicRootPath() + File.separator
				+ TEACHER_ICON + File.separator;
		Utils.mkDirs(dir);
		String url = dir + phone;
		return url;
	}

	private static Teacher toTeacher(JSONObject obj) throws JSONException {
		Teacher info = new Teacher();
		info.setServer_id(obj.getString("id"));
		info.setTimestamp(obj.getLong(JSONConstant.TIME_STAMP));
		info.setPhone(obj.getString("phone"));
		info.setName(obj.getString("name"));
		info.setWorkgroup(obj.getString("workgroup"));
		info.setWorkduty(obj.getString("workduty"));
		info.setHead_icon(obj.getString("portrait"));
		info.setShool_id(obj.getInt("school_id"));
		info.setBirthday(obj.getString("birthday"));
		info.setGender(obj.getInt("gender"));
		return info;
	}

	public static List<Teacher> toTeacherList(JSONArray array) {
		List<Teacher> list = new ArrayList<Teacher>();

		for (int i = 0; i < array.length(); i++) {
			try {
				JSONObject jsonObject = array.getJSONObject(i);
				list.add(toTeacher(jsonObject));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	public static String getPhones(List<Teacher> list) {
		StringBuffer buffer = new StringBuffer();
		for (Teacher teacher : list) {
			buffer.append(teacher.getPhone());
			buffer.append(ConstantValue.COMMON_SEPEARAOR);
		}
		String result = "";
		if (buffer.length() > 0) {
			result = buffer.substring(0, buffer.length() - 1);
		}
		return result;
	}

	@Override
	public String toString() {
		return "Teacher [id=" + id + ", name=" + name + ", head_icon="
				+ head_icon + ", birthday=" + birthday + ", timestamp="
				+ timestamp + ", server_id=" + server_id + ", workgroup="
				+ workgroup + ", workduty=" + workduty + ", phone=" + phone
				+ ", shool_id=" + shool_id + ", gender=" + gender + "]";
	}

}
