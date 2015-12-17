package com.cocobabys.dbmgr;

import java.util.ArrayList;
import java.util.List;

import com.cocobabys.dbmgr.info.NativeMediumInfo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class NativeMediumMgr {
	private SqliteHelper dbHelper;

	NativeMediumMgr(SqliteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	long addInfo(NativeMediumInfo info) {
		ContentValues values = buildInfo(info);
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		return writableDatabase.insertWithOnConflict(SqliteHelper.NATIVE_MEDIUM_URL_TAB, null, values,
				SQLiteDatabase.CONFLICT_IGNORE);
	}

	void addList(List<NativeMediumInfo> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		writableDatabase.beginTransaction(); // 手动设置开始事务

		try {
			for (NativeMediumInfo info : list) {
				ContentValues values = buildInfo(info);
				writableDatabase.insertWithOnConflict(SqliteHelper.NATIVE_MEDIUM_URL_TAB, null, values,
						SQLiteDatabase.CONFLICT_IGNORE);
			}
			// 数据插入操作循环
			writableDatabase.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		} catch (Exception e) {
			e.printStackTrace();
		}
		writableDatabase.endTransaction(); // 处理完成
	}

	private ContentValues buildInfo(NativeMediumInfo info) {
		ContentValues values = new ContentValues();
		values.put(NativeMediumInfo.KEY, info.getKey());
		values.put(NativeMediumInfo.VALUE, info.getValue());
		return values;
	}

	NativeMediumInfo getInfo(String key) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.NATIVE_MEDIUM_URL_TAB + " WHERE "
				+ NativeMediumInfo.KEY + " = '" + key + "'", null);

		NativeMediumInfo info = null;
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				info = getInfoByCursor(cursor);
				break;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return info;
	}

	List<NativeMediumInfo> getAllInfo() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.NATIVE_MEDIUM_URL_TAB, null);
		return getList(cursor);
	}
	

	private List<NativeMediumInfo> getList(Cursor cursor) {
		List<NativeMediumInfo> list = new ArrayList<NativeMediumInfo>();
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				NativeMediumInfo info = getInfoByCursor(cursor);
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
	
	void clear() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.NATIVE_MEDIUM_URL_TAB);
	}

	private NativeMediumInfo getInfoByCursor(Cursor cursor) {
		NativeMediumInfo info = new NativeMediumInfo();

		info.setId(cursor.getInt(0));
		info.setKey(cursor.getString(1));
		info.setValue(cursor.getString(2));
		return info;
	}
}
