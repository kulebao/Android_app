package com.cocobabys.dbmgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cocobabys.dbmgr.info.NewChatInfo;

class NewChatMgr {
	private SqliteHelper dbHelper;

	NewChatMgr(SqliteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	void addDataList(List<NewChatInfo> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		writableDatabase.beginTransaction(); // 手动设置开始事务

		try {
			for (NewChatInfo info : list) {
				ContentValues values = buildInfo(info);
				writableDatabase.insertWithOnConflict(SqliteHelper.NEW_CHAT_TAB, null, values,
						SQLiteDatabase.CONFLICT_REPLACE);
			}
			// 数据插入操作循环
			writableDatabase.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		} finally {
			writableDatabase.endTransaction(); // 处理完成
		}
	}

	void clear() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.NEW_CHAT_TAB);
	}

	// 返回最多max条chat记录，按照timestamp倒序，再将此list倒序排列
	List<NewChatInfo> getChatInfoWithLimite(int max, String childid) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.NEW_CHAT_TAB + " WHERE " + NewChatInfo.CHILD_ID
				+ "='" + childid + "' ORDER BY " + NewChatInfo.TIMESTAMP + " DESC LIMIT " + max, null);
		List<NewChatInfo> list = getChatInfoList(cursor);
		if (!list.isEmpty()) {
			Collections.reverse(list);
		}
		return list;
	}

	// 返回小于to的所有chat，最多max条，按照timestamp倒序，再将此list倒序排列
	List<NewChatInfo> getChatInfoWithLimite(int max, long to, String childid) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.NEW_CHAT_TAB + " WHERE " + NewChatInfo.CHILD_ID
				+ "='" + childid + "' AND " + NewChatInfo.TIMESTAMP + " < " + to + " ORDER BY " + NewChatInfo.TIMESTAMP
				+ " DESC LIMIT " + max, null);
		List<NewChatInfo> list = getChatInfoList(cursor);
		if (!list.isEmpty()) {
			Collections.reverse(list);
		}
		return list;
	}

	// 返回该小孩最后一条家户互动记录
	NewChatInfo getLastChatInfo(String childid) {
		NewChatInfo info = null;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.NEW_CHAT_TAB + " WHERE " + NewChatInfo.CHILD_ID
				+ "='" + childid + "' ORDER BY " + NewChatInfo.CHAT_ID + " DESC LIMIT " + 1, null);

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

	private List<NewChatInfo> getChatInfoList(Cursor cursor) {
		List<NewChatInfo> list = new ArrayList<NewChatInfo>();
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				NewChatInfo info = getDataByCursor(cursor);
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

	private ContentValues buildInfo(NewChatInfo info) {
		ContentValues values = new ContentValues();
		values.put(NewChatInfo.CHAT_ID, info.getChat_id());
		values.put(NewChatInfo.CHILD_ID, info.getChild_id());
		values.put(NewChatInfo.CONTENT, info.getContent());
		values.put(NewChatInfo.MEDIA_TYPE, info.getMedia_type());
		values.put(NewChatInfo.MEDIA_URL, info.getMedia_url());
		values.put(NewChatInfo.SENDER_ID, info.getSender_id());
		values.put(NewChatInfo.SENDER_TYPE, info.getSender_type());
		values.put(NewChatInfo.TIMESTAMP, info.getTimestamp());
		return values;
	}

	private NewChatInfo getDataByCursor(Cursor cursor) {
		NewChatInfo info = new NewChatInfo();

		info.setId(cursor.getInt(0));
		info.setChat_id(cursor.getLong(1));
		info.setChild_id(cursor.getString(2));
		info.setContent(cursor.getString(3));
		info.setMedia_type(cursor.getString(4));
		info.setMedia_url(cursor.getString(5));
		info.setSender_id(cursor.getString(6));
		info.setSender_type(cursor.getString(7));
		info.setTimestamp(cursor.getLong(8));
		return info;
	}
}
