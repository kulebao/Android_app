package com.cocobabys.dbmgr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.cocobabys.bean.GroupExpInfo;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.dbmgr.info.ExpInfo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class ExpMgr {
	private SqliteHelper dbHelper;

	ExpMgr(SqliteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	void addDataList(List<ExpInfo> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		writableDatabase.beginTransaction(); // 手动设置开始事务

		try {
			for (ExpInfo info : list) {
				ContentValues values = buildInfo(info);
				writableDatabase.insertWithOnConflict(SqliteHelper.EXP_TAB,
						null, values, SQLiteDatabase.CONFLICT_IGNORE);
			}
			// 数据插入操作循环
			writableDatabase.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		} finally {
			writableDatabase.endTransaction(); // 处理完成
		}
	}

	void clear() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.EXP_TAB);
	}

	List<GroupExpInfo> getExpCountGroupByMonthPerYear(int year, String childID) {
		List<GroupExpInfo> list = new ArrayList<GroupExpInfo>();

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db
				.rawQuery(
						"SELECT month_name, COUNT(exp_id) AS total, "
								+ "  STRFTIME('%m', timestamp / 1000, 'unixepoch') AS z"
								+ " FROM month_tab LEFT JOIN "
								+ SqliteHelper.EXP_TAB
								+ " ON "
								+ " z = month_tab.month_name "
								+ " AND "
								+ ExpInfo.CHILD_ID
								+ " = '"
								+ childID
								+ "'"
								+ " AND STRFTIME('%Y', timestamp / 1000, 'unixepoch') "
								+ "='" + year + "' GROUP BY month_name", null);

		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				GroupExpInfo info = getGroupInfo(cursor);
				list.add(info);
				cursor.moveToNext();
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		addIconPath(list, year, childID);
		return list;
	}

	// 获取每个月，最晚时间的照片，且照片必须存在
	private void addIconPath(List<GroupExpInfo> list, int year, String childID) {
		for (GroupExpInfo info : list) {
			String month = info.getMonth();
			List<ExpInfo> tmpList = getExpInfoByMonthAndYear(year + "-" + month);
			FindIcon: for (int i = 0; i < tmpList.size(); i++) {
				// 先找非缩略图的
				ExpInfo expInfo = tmpList.get(i);
				List<String> localUrls = expInfo.getLocalUrls(false);

				// 是图片资源才找原图，视频资源只找缩略图
				if (expInfo.getMediumType().equals(JSONConstant.IMAGE_TYPE)) {
					for (String path : localUrls) {
						if (new File(path).exists()) {
							info.setIconpath(path);
							break FindIcon;
						}
					}
				}

				// 没有，再去找缩略图
				localUrls = expInfo.getLocalUrls(true);
				for (String path : localUrls) {
					if (new File(path).exists()) {
						info.setIconpath(path);
						break FindIcon;
					}
				}
			}

		}
	}

	int getExpCountInMonth(int year, String month, String childID) {
		int count = 0;

		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT COUNT(exp_id) AS total FROM "
				+ SqliteHelper.EXP_TAB + " WHERE " + ExpInfo.CHILD_ID + " = '"
				+ childID + "'"
				+ " AND STRFTIME('%Y', timestamp / 1000, 'unixepoch') " + "='"
				+ year
				+ "' AND STRFTIME('%m', timestamp / 1000, 'unixepoch') = '"
				+ month + "'", null);

		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				count = cursor.getInt(0);
				break;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return count;
	}

	private GroupExpInfo getGroupInfo(Cursor cursor) {
		String month = cursor.getString(0);
		int count = cursor.getInt(1);
		GroupExpInfo info = new GroupExpInfo();
		info.setCount(count);
		info.setMonth(month);
		return info;
	}

	// monthAndYear 必须是2014-05这种模式，注意短横线和数字前面的0
	List<ExpInfo> getExpInfoByMonthAndYear(String monthAndYear) {
		String childid = DataMgr.getInstance().getSelectedChild()
				.getServer_id();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * " + " FROM "
				+ SqliteHelper.EXP_TAB + " WHERE "
				+ " STRFTIME('%Y-%m', timestamp / 1000, 'unixepoch') " + "='"
				+ monthAndYear + "'" + " AND " + ExpInfo.CHILD_ID + " = '"
				+ childid + "' ORDER BY timestamp DESC", null);

		return getExpInfoList(cursor);
	}

	ExpInfo getExpInfoByID(long expid) {
		ExpInfo info = null;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * " + " FROM "
				+ SqliteHelper.EXP_TAB + " WHERE " + ExpInfo.EXP_ID + " = "
				+ expid, null);

		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				info = getDataByCursor(cursor);
				break;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return info;
	}

	void deleteExp(long expid) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.EXP_TAB + " WHERE "
				+ ExpInfo.EXP_ID + " = " + expid);
	}

	private List<ExpInfo> getExpInfoList(Cursor cursor) {
		List<ExpInfo> list = new ArrayList<ExpInfo>();
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				ExpInfo info = getDataByCursor(cursor);
				list.add(info);
				cursor.moveToNext();
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	private ContentValues buildInfo(ExpInfo info) {
		ContentValues values = new ContentValues();
		values.put(ExpInfo.EXP_ID, info.getExp_id());
		values.put(ExpInfo.CHILD_ID, info.getChild_id());
		values.put(ExpInfo.CONTENT, info.getContent());
		values.put(ExpInfo.MEDIUM, info.getMedium());
		values.put(ExpInfo.SENDER_ID, info.getSender_id());
		values.put(ExpInfo.SENDER_TYPE, info.getSender_type());
		values.put(ExpInfo.TIMESTAMP, info.getTimestamp());
		return values;
	}

	private ExpInfo getDataByCursor(Cursor cursor) {
		ExpInfo info = new ExpInfo();

		info.setId(cursor.getInt(0));
		info.setExp_id(cursor.getLong(1));
		info.setChild_id(cursor.getString(2));
		info.setContent(cursor.getString(3));
		info.setMedium(cursor.getString(4));
		info.setSender_id(cursor.getString(5));
		info.setSender_type(cursor.getString(6));
		info.setTimestamp(cursor.getLong(7));
		return info;
	}
}
