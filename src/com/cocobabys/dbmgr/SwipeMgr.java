package com.cocobabys.dbmgr;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cocobabys.constant.JSONConstant;
import com.cocobabys.dbmgr.info.SwipeInfo;

class SwipeMgr {
	private SqliteHelper dbHelper;

	SwipeMgr(SqliteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	long addData(SwipeInfo info) {
		ContentValues values = buildInfo(info);
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		return writableDatabase.insertWithOnConflict(SqliteHelper.SWIPE_TAB,
				null, values, SQLiteDatabase.CONFLICT_IGNORE);
	}

	private ContentValues buildInfo(SwipeInfo info) {
		ContentValues values = new ContentValues();
		values.put(SwipeInfo.TIMESTAMP, info.getTimestamp());
		values.put(SwipeInfo.TYPE, info.getType());
		values.put(SwipeInfo.CHILD_ID, info.getChild_id());
		values.put(SwipeInfo.ICON_URL, info.getUrl());
		values.put(SwipeInfo.PARENT_NAME, info.getParent_name());
		return values;
	}

	void addDataList(List<SwipeInfo> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		writableDatabase.beginTransaction(); // 手动设置开始事务

		for (SwipeInfo info : list) {
			ContentValues values = buildInfo(info);
			writableDatabase.insertWithOnConflict(SqliteHelper.SWIPE_TAB, null,
					values, SQLiteDatabase.CONFLICT_IGNORE);
		}
		// 数据插入操作循环
		writableDatabase.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		writableDatabase.endTransaction(); // 处理完成
	}

	SwipeInfo getDataByID(int id) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.SWIPE_TAB
				+ " WHERE " + SwipeInfo.ID + " = " + id, null);
		List<SwipeInfo> list = getDataList(cursor);
		return list.isEmpty() ? null : list.get(0);
	}

	SwipeInfo getDataByTimeStamp(long timestamp) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.SWIPE_TAB
				+ " WHERE " + SwipeInfo.TIMESTAMP + " = " + timestamp, null);
		List<SwipeInfo> list = getDataList(cursor);
		return list.isEmpty() ? null : list.get(0);
	}

	// 获取某天的，最晚的刷卡入园记录，如果没有返回"" 且只能显示当前选中孩子的信息
	String getLastestSwipeIn(String date, String childid) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		long startLimitTime = Date.valueOf(date).getTime();
		long endLimitTime = startLimitTime + 24L * 60 * 60 * 1000L;
		String sql = "SELECT * FROM " + SqliteHelper.SWIPE_TAB + " WHERE "
				+ SwipeInfo.TYPE + " = "
				+ JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKIN + " AND "
				+ SwipeInfo.CHILD_ID + " = '" + childid + "' AND "
				+ SwipeInfo.TIMESTAMP + " >= " + startLimitTime + " AND "
				+ SwipeInfo.TIMESTAMP + " < " + endLimitTime + " ORDER BY "
				+ SwipeInfo.TIMESTAMP + " DESC LIMIT 1";
		Cursor cursor = db.rawQuery(sql, null);

		List<SwipeInfo> list = getDataList(cursor);
		return list.isEmpty() ? "" : list.get(0).getFormattedTime();
	}

	// 获取某天的，最晚的刷卡离园记录，如果没有返回"" 且只能显示当前选中孩子的信息
	String getLatestSwipeOut(String date, String childid) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		long startLimitTime = Date.valueOf(date).getTime();
		long endLimitTime = startLimitTime + 24L * 60 * 60 * 1000L;
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.SWIPE_TAB
				+ " WHERE " + SwipeInfo.TYPE + " = "
				+ JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKOUT + " AND "
				+ SwipeInfo.CHILD_ID + " = '" + childid + "' AND "
				+ SwipeInfo.TIMESTAMP + " >= " + startLimitTime + " AND "
				+ SwipeInfo.TIMESTAMP + " < " + endLimitTime + " ORDER BY "
				+ SwipeInfo.TIMESTAMP + " DESC LIMIT 1", null);
		List<SwipeInfo> list = getDataList(cursor);
		return list.isEmpty() ? "" : list.get(0).getFormattedTime();
	}

	// 获取某天的，全部刷卡记录，其中date的格式必须满足yyyy-mm-dd形式 且只能显示当前选中孩子的信息
	List<SwipeInfo> getAllSwipeCardNotice(String date, String childid) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		long startLimitTime = Date.valueOf(date).getTime();
		long endLimitTime = startLimitTime + 24L * 60 * 60 * 1000L;

		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.SWIPE_TAB
				+ " WHERE " + SwipeInfo.CHILD_ID + " = '" + childid + "' AND "
				+ SwipeInfo.TIMESTAMP + " >= " + startLimitTime + " AND "
				+ SwipeInfo.TIMESTAMP + " < " + endLimitTime + " ORDER BY "
				+ SwipeInfo.TIMESTAMP + " DESC", null);

		return getDataList(cursor);
	}

	private List<SwipeInfo> getDataList(Cursor cursor) {
		List<SwipeInfo> list = new ArrayList<SwipeInfo>();
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				SwipeInfo info = getDataByCursor(cursor);
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

	private SwipeInfo getDataByCursor(Cursor cursor) {
		SwipeInfo info = new SwipeInfo();

		info.setId(cursor.getInt(0));
		info.setTimestamp(cursor.getLong(1));
		info.setType(cursor.getInt(2));
		info.setChild_id(cursor.getString(3));
		info.setUrl(cursor.getString(4));
		info.setParent_name(cursor.getString(5));
		return info;
	}
}
