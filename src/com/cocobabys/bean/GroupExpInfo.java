package com.cocobabys.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cocobabys.R;

public class GroupExpInfo {
	// like 01 ,02 ,11,12...
	private String month = "";
	private int count = 0;
	// 当月最近一张图片本地路径
	private String iconpath = "";

	private static Map<String, Integer> iconMap = new HashMap<String, Integer>() {
		{
			put("01", R.drawable.m1);
			put("02", R.drawable.m2);
			put("03", R.drawable.m3);
			put("04", R.drawable.m4);
			put("05", R.drawable.m5);
			put("06", R.drawable.m6);
			put("07", R.drawable.m7);
			put("08", R.drawable.m8);
			put("09", R.drawable.m9);
			put("10", R.drawable.m10);
			put("11", R.drawable.m11);
			put("12", R.drawable.m12);
		}
	};

	private static Map<String, String> nameMap = new HashMap<String, String>() {
		{
			put("01", "1月");
			put("02", "2月");
			put("03", "3月");
			put("04", "4月");
			put("05", "5月");
			put("06", "6月");
			put("07", "7月");
			put("08", "8月");
			put("09", "9月");
			put("10", "10月");
			put("11", "11月");
			put("12", "12月");
		}
	};

	public String getMonthName() {
		return nameMap.get(month);
	}

	public static Map<String, Integer> getIconMap() {
		return iconMap;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getIconpath() {
		return iconpath;
	}

	public void setIconpath(String iconpath) {
		this.iconpath = iconpath;
	}

	// 必须满足1~12月份的数据，供UI显示，没有需要补0，服务器只返回有数据的月份，所以这里要处理一下
	public static List<GroupExpInfo> jsonArrayToGroupExpInfoList(JSONArray array) throws JSONException {
		List<GroupExpInfo> list = initData();

		for (int i = 0; i < array.length(); i++) {
			JSONObject object = array.getJSONObject(i);
			String date = object.getString("month");
			// 返回最后2个字符，是代表月份
			String month = date.substring(date.length() - 2, date.length());
			int count = object.getInt("count");

			for (GroupExpInfo groupExpInfo : list) {
				if (groupExpInfo.getMonth().equals(month)) {
					groupExpInfo.setCount(count);
				}
			}
		}

		return list;
	}

	private static ArrayList<GroupExpInfo> initData() {
		ArrayList<GroupExpInfo> list = new ArrayList<GroupExpInfo>();
		for (int i = 1; i < 13; i++) {
			GroupExpInfo info = new GroupExpInfo();
			String month = "";
			if (i < 10) {
				month = "0" + i;
			} else {
				month = "" + i;
			}
			info.setMonth(month);
			list.add(info);
		}
		return list;
	}

}
