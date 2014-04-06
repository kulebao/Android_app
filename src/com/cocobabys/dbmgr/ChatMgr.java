package com.cocobabys.dbmgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cocobabys.dbmgr.info.ChatInfo;

class ChatMgr {
	private SqliteHelper dbHelper;

	ChatMgr(SqliteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	long addChatInfo(ChatInfo info) {
		ContentValues values = buildChatInfo(info);
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		return writableDatabase.insertWithOnConflict(SqliteHelper.CHAT_TAB, null, values,
				SQLiteDatabase.CONFLICT_IGNORE);
	}

	private ContentValues buildChatInfo(ChatInfo info) {
		ContentValues values = new ContentValues();
		values.put(ChatInfo.SENDER, info.getSender());
		values.put(ChatInfo.CONTENT, info.getContent());
		values.put(ChatInfo.TIMESTAMP, info.getTimestamp());
		values.put(ChatInfo.ICON_URL, info.getIcon_url());
		values.put(ChatInfo.SERVER_ID, info.getServer_id());
		values.put(ChatInfo.SEND_RESULT, info.getSend_result());
		values.put(ChatInfo.PHONE, info.getPhone());
		return values;
	}

	void addChatInfoList(List<ChatInfo> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		writableDatabase.beginTransaction(); // 手动设置开始事务

		for (ChatInfo info : list) {
			ContentValues values = buildChatInfo(info);
			writableDatabase.insertWithOnConflict(SqliteHelper.CHAT_TAB, null, values, SQLiteDatabase.CONFLICT_IGNORE);
		}
		// 数据插入操作循环
		writableDatabase.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		writableDatabase.endTransaction(); // 处理完成
	}

	ChatInfo getChatInfoByID(int id) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.CHAT_TAB + " WHERE " + ChatInfo.ID + " = " + id,
				null);
		List<ChatInfo> list = getChatInfoList(cursor);
		return list.isEmpty() ? null : list.get(0);
	}

	// 返回最多max条chat记录，按照timestamp倒序，再将此list倒序排列
	List<ChatInfo> getChatInfoWithLimite(int max) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.CHAT_TAB + " ORDER BY " + ChatInfo.TIMESTAMP
				+ " DESC LIMIT " + max, null);
		List<ChatInfo> list = getChatInfoList(cursor);
		if (!list.isEmpty()) {
			Collections.reverse(list);
		}
		return list;
	}

	// 返回小于to的所有chat，最多max条，按照timestamp倒序，再将此list倒序排列
	List<ChatInfo> getChatInfoWithLimite(int max, long to) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.CHAT_TAB + " WHERE " + ChatInfo.TIMESTAMP + " < "
				+ to + " ORDER BY " + ChatInfo.TIMESTAMP + " DESC LIMIT " + max, null);
		List<ChatInfo> list = getChatInfoList(cursor);
		if (!list.isEmpty()) {
			Collections.reverse(list);
		}
		return list;
	}

	int getLastServerid() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.CHAT_TAB + " ORDER BY " + ChatInfo.SERVER_ID
				+ " DESC LIMIT " + 1, null);
		List<ChatInfo> list = getChatInfoList(cursor);

		return list.isEmpty() ? 0 : list.get(0).getServer_id();
	}

	void removeAllChatInfo() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.CHAT_TAB);
	}

	private List<ChatInfo> getChatInfoList(Cursor cursor) {
		List<ChatInfo> list = new ArrayList<ChatInfo>();
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				ChatInfo info = getChatInfoByCursor(cursor);
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

	private ChatInfo getChatInfoByCursor(Cursor cursor) {
		ChatInfo info = new ChatInfo();

		info.setId(cursor.getInt(0));
		info.setSender(cursor.getString(1));
		info.setContent(cursor.getString(2));
		info.setTimestamp(cursor.getLong(3));
		info.setIcon_url(cursor.getString(4));
		info.setServer_id(cursor.getInt(5));
		info.setSend_result(cursor.getInt(6));
		info.setPhone(cursor.getString(7));
		return info;
	}
}
