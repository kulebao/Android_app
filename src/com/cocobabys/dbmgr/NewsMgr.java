package com.cocobabys.dbmgr;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cocobabys.dbmgr.info.News;

class NewsMgr {
	private SqliteHelper dbHelper;

	NewsMgr(SqliteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	long addNews(News info) {
		ContentValues values = buildNewsInfo(info);
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		return writableDatabase.insertWithOnConflict(SqliteHelper.NEWS_TAB,
				null, values, SQLiteDatabase.CONFLICT_REPLACE);
	}

	private ContentValues buildNewsInfo(News info) {
		ContentValues values = new ContentValues();
		values.put(News.TITLE, info.getTitle());
		values.put(News.CONTENT, info.getContent());
		values.put(News.TIMESTAMP, info.getTimestamp());
		values.put(News.NEWS_TYPE, info.getType());
		values.put(News.PUBLISHER, info.getPublisher());
		values.put(News.NEWS_SERVER_ID, info.getNews_server_id());
		values.put(News.ICON_URL, info.getIcon_url());
		values.put(News.CLASS_ID, info.getClass_id());
		values.put(News.NEED_RECEIPT, info.getNeed_receipt());
		return values;
	}

	void addNewsList(List<News> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		writableDatabase.beginTransaction(); // 手动设置开始事务

		try {
			for (News info : list) {
				ContentValues values = buildNewsInfo(info);
				writableDatabase.insertWithOnConflict(SqliteHelper.NEWS_TAB,
						null, values, SQLiteDatabase.CONFLICT_REPLACE);
			}
			// 数据插入操作循环
			writableDatabase.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		} catch (Exception e) {
			e.printStackTrace();
		}
		writableDatabase.endTransaction(); // 处理完成
	}

	News getNewsByID(int id) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.NEWS_TAB
				+ " WHERE " + News.ID + " = " + id, null);
		List<News> list = getNewsList(cursor);
		return list.isEmpty() ? null : list.get(0);
	}

	List<News> getAllNews() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.NEWS_TAB
				+ " ORDER BY " + News.TIMESTAMP + " DESC", null);

		return getNewsList(cursor);
	}

	List<News> getAllNewsByType(int type) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.NEWS_TAB
				+ " WHERE " + News.NEWS_TYPE + " = " + type + " ORDER BY "
				+ News.TIMESTAMP + " DESC", null);

		return getNewsList(cursor);
	}

	List<News> getNewsByType(int type, int max) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.NEWS_TAB
				+ " WHERE " + News.NEWS_TYPE + " = " + type + " ORDER BY "
				+ News.TIMESTAMP + " DESC LIMIT " + max, null);

		return getNewsList(cursor);
	}

	void removeAllNewsByType(int type) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.NEWS_TAB + " WHERE "
				+ News.NEWS_TYPE + " = " + type);
	}

	private List<News> getNewsList(Cursor cursor) {
		List<News> list = new ArrayList<News>();
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				News info = getNewsByCursor(cursor);
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

	private News getNewsByCursor(Cursor cursor) {
		News info = new News();

		info.setId(cursor.getInt(0));
		info.setTitle(cursor.getString(1));
		info.setContent(cursor.getString(2));
		info.setTimestamp(cursor.getLong(3));
		info.setType(cursor.getInt(4));
		info.setNews_server_id(cursor.getInt(5));
		info.setPublisher(cursor.getString(6));
		info.setIcon_url(cursor.getString(7));
		info.setClass_id(cursor.getInt(8));
		info.setNeed_receipt(cursor.getInt(9));
		return info;
	}
}
