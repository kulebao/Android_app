package com.cocobabys.dbmgr;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.cocobabys.dbmgr.info.ChildInfo;
import com.cocobabys.dbmgr.info.InfoHelper;

public class ChildrenInfoMgr {
	private SqliteHelper dbHelper;

	public ChildrenInfoMgr(SqliteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	long addChildrenInfo(ChildInfo info) {
		ContentValues values = setProp(info);
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		return writableDatabase.insert(SqliteHelper.CHILDREN_INFO_TAB, null,
				values);
	}

	private ContentValues setProp(ChildInfo info) {
		ContentValues values = new ContentValues();
		values.put(ChildInfo.CHILD_BIRTHDAY, info.getChild_birthday());
		values.put(ChildInfo.CHILD_LOCAL_HEAD_ICON, info.getLocal_url());
		values.put(ChildInfo.CHILD_NICK_NAME, info.getChild_nick_name());
		values.put(ChildInfo.CHILD_SERVER_HEAD_ICON, info.getServer_url());
		values.put(ChildInfo.SERVER_ID, info.getServer_id());
		values.put(InfoHelper.TIMESTAMP, info.getTimestamp());
		values.put(ChildInfo.SELECTED, info.getSelected());
		values.put(ChildInfo.CLASS_ID, info.getClass_id());
		values.put(ChildInfo.CLASS_NAME, info.getClass_name());
		values.put(ChildInfo.CHILD_NAME, info.getChild_name());
		values.put(ChildInfo.GENDER, info.getGender());
		return values;
	}

	void addChildrenInfoList(List<ChildInfo> list) {
		for (ChildInfo info : list) {
			addChildrenInfo(info);
		}
	}

	void updateLocalUrl(String serverid, String localurl) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ChildInfo.CHILD_LOCAL_HEAD_ICON, localurl);
		db.update(SqliteHelper.CHILDREN_INFO_TAB, values, ChildInfo.SERVER_ID
				+ "  = '" + serverid + "'", null);
	}

	void updateNick(String serverid, String nick) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ChildInfo.CHILD_NICK_NAME, nick);
		db.update(SqliteHelper.CHILDREN_INFO_TAB, values, ChildInfo.SERVER_ID
				+ "  = '" + serverid + "'", null);
	}

	void updateBirthday(String serverid, long birthday) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ChildInfo.CHILD_BIRTHDAY, birthday);
		db.update(SqliteHelper.CHILDREN_INFO_TAB, values, ChildInfo.SERVER_ID
				+ "  = '" + serverid + "'", null);
	}

	void updateChildInfo(String serverid, ChildInfo info) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = setProp(info);
		db.update(SqliteHelper.CHILDREN_INFO_TAB, values, ChildInfo.SERVER_ID
				+ "  = '" + serverid + "'", null);
	}

	int setSelectedChild(String server_id) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ChildInfo.SELECTED, 0);
		// 先把所有小孩置为未选中
		db.update(SqliteHelper.CHILDREN_INFO_TAB, values, null, null);

		// 再把需要的小孩置为选中
		values.put(ChildInfo.SELECTED, 1);
		return db.update(SqliteHelper.CHILDREN_INFO_TAB, values,
				ChildInfo.SERVER_ID + "  = '" + server_id + "'", null);
	}

	List<ChildInfo> getAllChildrenInfo() {
		List<ChildInfo> list = new ArrayList<ChildInfo>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ SqliteHelper.CHILDREN_INFO_TAB, null);
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				ChildInfo info = getChildrenInfoByCursor(cursor);
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

	// 获取当前选中的孩子
	ChildInfo getSelectedChild() {
		ChildInfo info = null;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ SqliteHelper.CHILDREN_INFO_TAB + " WHERE "
				+ ChildInfo.SELECTED + " = 1", null);
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				info = getChildrenInfoByCursor(cursor);
				cursor.moveToNext();
				break;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return info;
	}

	// 获取全部，不同的班级id
	List<String> getAllClassID() {
		List<String> list = new ArrayList<String>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT DISTINCT(" + ChildInfo.CLASS_ID
				+ ") FROM " + SqliteHelper.CHILDREN_INFO_TAB, null);
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(0) != null)) {
				list.add(cursor.getString(0));
				cursor.moveToNext();
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return list;
	}

	ChildInfo getChildByID(String id) {
		ChildInfo info = null;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ SqliteHelper.CHILDREN_INFO_TAB + " WHERE "
				+ ChildInfo.SERVER_ID + " = '" + id + "'", null);
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				info = getChildrenInfoByCursor(cursor);
				cursor.moveToNext();
				break;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return info;
	}

	String getLatestTimestamp() {
		String time = "";
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT MAX(" + InfoHelper.TIMESTAMP
				+ ") FROM " + SqliteHelper.CHILDREN_INFO_TAB, null);
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(0) != null)) {
				time = cursor.getString(0);
				cursor.moveToNext();
				break;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return time;
	}

	String getClassNameByClassID(int classid) {
		String name = "";
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT " + ChildInfo.CLASS_NAME + " FROM "
				+ SqliteHelper.CHILDREN_INFO_TAB + " WHERE "
				+ ChildInfo.CLASS_ID + " = '" + classid + "'", null);
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(0) != null)) {
				name = cursor.getString(0);
				cursor.moveToNext();
				break;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return name;
	}

	void clearChildInfo() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.execSQL("DELETE FROM " + SqliteHelper.CHILDREN_INFO_TAB);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private ChildInfo getChildrenInfoByCursor(Cursor cursor) {
		ChildInfo info = new ChildInfo();

		info.setId(cursor.getInt(0));
		info.setChild_nick_name(cursor.getString(1));
		info.setLocal_url(cursor.getString(2));
		info.setServer_url(cursor.getString(3));
		info.setChild_birthday(cursor.getLong(4));
		info.setSelected(cursor.getInt(5));
		info.setTimestamp(cursor.getLong(6));
		info.setServer_id(cursor.getString(7));
		info.setClass_id(cursor.getString(8));
		info.setClass_name(cursor.getString(9));
		info.setChild_name(cursor.getString(10));
		info.setGender(cursor.getInt(11));
		return info;
	}
}
