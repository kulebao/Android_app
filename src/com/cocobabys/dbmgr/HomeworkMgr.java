package com.cocobabys.dbmgr;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cocobabys.dbmgr.info.Homework;

class HomeworkMgr {
	private SqliteHelper dbHelper;

	HomeworkMgr(SqliteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	long addHomework(Homework info) {
		ContentValues values = buildHomework(info);
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		return writableDatabase.insertWithOnConflict(SqliteHelper.HOMEWORK_TAB,
				null, values, SQLiteDatabase.CONFLICT_REPLACE);
	}

	private ContentValues buildHomework(Homework info) {
		ContentValues values = new ContentValues();
		values.put(Homework.TITLE, info.getTitle());
		values.put(Homework.CONTENT, info.getContent());
		values.put(Homework.TIMESTAMP, info.getTimestamp());
		values.put(Homework.ICON_URL, info.getIcon_url());
		values.put(Homework.SERVER_ID, info.getServer_id());
		values.put(Homework.PUBLISHER, info.getPublisher());
		values.put(Homework.CLASS_ID, info.getClass_id());
		return values;
	}

	void addHomeworkList(List<Homework> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		writableDatabase.beginTransaction(); // 手动设置开始事务

		for (Homework info : list) {
			ContentValues values = buildHomework(info);
			writableDatabase.insertWithOnConflict(SqliteHelper.HOMEWORK_TAB,
					null, values, SQLiteDatabase.CONFLICT_REPLACE);
		}
		// 数据插入操作循环
		writableDatabase.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		writableDatabase.endTransaction(); // 处理完成
	}

	Homework getHomeworkByID(int id) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ SqliteHelper.HOMEWORK_TAB + " WHERE " + Homework.ID + " = "
				+ id, null);
		List<Homework> list = getHomeworkList(cursor);
		return list.isEmpty() ? null : list.get(0);
	}

	List<Homework> getHomeworkWithLimite(int max) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ SqliteHelper.HOMEWORK_TAB + " ORDER BY " + Homework.TIMESTAMP
				+ " DESC LIMIT " + max, null);

		return getHomeworkList(cursor);
	}

	void removeAllHomework() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.HOMEWORK_TAB);
	}

	private List<Homework> getHomeworkList(Cursor cursor) {
		List<Homework> list = new ArrayList<Homework>();
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				Homework info = getHomeworkByCursor(cursor);
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

	private Homework getHomeworkByCursor(Cursor cursor) {
		Homework info = new Homework();

		info.setId(cursor.getInt(0));
		info.setTitle(cursor.getString(1));
		info.setContent(cursor.getString(2));
		info.setTimestamp(cursor.getLong(3));
		info.setIcon_url(cursor.getString(4));
		info.setServer_id(cursor.getInt(5));
		info.setClass_id(cursor.getInt(6));
		info.setPublisher(cursor.getString(7));
		return info;
	}
}
