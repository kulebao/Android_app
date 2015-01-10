package com.cocobabys.dbmgr.info;

import java.util.List;

public class NativeMediumInfo {
	public static final String ID = "_id";
	public static final String KEY = "key";
	public static final String VALUE = "value";

	private long id = 0;
	private String key = "";
	private String value = "";

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String toValue(List<String> list) {
		StringBuffer value = new StringBuffer();

		for (String string : list) {
			value.append(string);
			value.append(",");
		}

		// 去掉最后一个逗号
		return value.substring(0, value.length() - 1);
	}

	@Override
	public String toString() {
		return "NativeMediumInfo [id=" + id + ", key=" + key + ", value=" + value + "]";
	}

}
