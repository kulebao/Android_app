package com.cocobabys.dbmgr.info;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.text.TextUtils;

import com.cocobabys.R;
import com.cocobabys.activities.MyApplication;
import com.cocobabys.bean.ScheduleListItem;
import com.cocobabys.utils.Utils;

public class ScheduleInfo {
	public static final String ID = "_id";
	public static final String SCHEDULE_ID = "schedule_id";
	public static final String SCHEDULE_CONTENT = "schedule_content";
	private String schedule_id = "";
	private String schedule_content = "";
	private String timestamp = "";
	private int id = 0;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSchedule_id() {
		return schedule_id;
	}

	public void setSchedule_id(String schedule_id) {
		this.schedule_id = schedule_id;
	}

	public String getSchedule_content() {
		return schedule_content;
	}

	public void setSchedule_content(String schedule_content) {
		this.schedule_content = schedule_content;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public List<ScheduleListItem> getScheduleListItemList() {
		Resources resources = MyApplication.getInstance().getResources();
		List<ScheduleListItem> list = new ArrayList<ScheduleListItem>();
		if (!TextUtils.isEmpty(schedule_content)) {
			JSONObject object;
			try {
				// 计算出本周星期一的日历时间
				Calendar cal = Utils.getMonDayCalendar();

				object = new JSONObject(schedule_content);

				String oneday = getDetail(object, InfoHelper.MON);
				if (!TextUtils.isEmpty(oneday)) {
					ScheduleListItem item = getItem(oneday);
					item.setDayofweek(resources.getString(R.string.mon));
					item.setDate(InfoHelper.getYearMonthDayFormat().format(
							cal.getTime()));
					list.add(item);
				}

				oneday = getDetail(object, InfoHelper.TUE);
				if (!TextUtils.isEmpty(oneday)) {
					ScheduleListItem item = getItem(oneday);
					item.setDayofweek(resources.getString(R.string.tue));
					cal.add(Calendar.DATE, 1);
					item.setDate(InfoHelper.getYearMonthDayFormat().format(
							cal.getTime()));
					list.add(item);
				}

				oneday = getDetail(object, InfoHelper.WED);
				if (!TextUtils.isEmpty(oneday)) {
					ScheduleListItem item = getItem(oneday);
					item.setDayofweek(resources.getString(R.string.wed));
					cal.add(Calendar.DATE, 1);
					item.setDate(InfoHelper.getYearMonthDayFormat().format(
							cal.getTime()));
					list.add(item);
				}

				oneday = getDetail(object, InfoHelper.THU);
				if (!TextUtils.isEmpty(oneday)) {
					ScheduleListItem item = getItem(oneday);
					item.setDayofweek(resources.getString(R.string.thu));
					cal.add(Calendar.DATE, 1);
					item.setDate(InfoHelper.getYearMonthDayFormat().format(
							cal.getTime()));
					list.add(item);
				}

				oneday = getDetail(object, InfoHelper.FRI);
				if (!TextUtils.isEmpty(oneday)) {
					ScheduleListItem item = getItem(oneday);
					item.setDayofweek(resources.getString(R.string.fri));
					cal.add(Calendar.DATE, 1);
					item.setDate(InfoHelper.getYearMonthDayFormat().format(
							cal.getTime()));
					list.add(item);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	public String getDetail(JSONObject object, String key) {
		String oneday = "";
		try {
			oneday = object.getString(key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return oneday;
	}

	public ScheduleListItem getItem(String oneday) throws JSONException {
		ScheduleListItem item = new ScheduleListItem();
		JSONObject onedaydetail = new JSONObject(oneday);
		item.setMorningContent(onedaydetail.getString(InfoHelper.AM));
		item.setAfternoonContent(onedaydetail.getString(InfoHelper.PM));
		return item;
	}

}
