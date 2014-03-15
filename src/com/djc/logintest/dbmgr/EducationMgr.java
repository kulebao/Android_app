package com.djc.logintest.dbmgr;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.djc.logintest.dbmgr.info.EducationInfo;

class EducationMgr {
	private SqliteHelper dbHelper;

	EducationMgr(SqliteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	long addEduRecord(EducationInfo info) {
		ContentValues values = buildInfo(info);
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		return writableDatabase.insertWithOnConflict(
				SqliteHelper.EDUCATION_TAB, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	void addEduRecordList(List<EducationInfo> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		writableDatabase.beginTransaction(); // 手动设置开始事务

		for (EducationInfo info : list) {
			ContentValues values = buildInfo(info);
			writableDatabase.insertWithOnConflict(SqliteHelper.EDUCATION_TAB,
					null, values, SQLiteDatabase.CONFLICT_REPLACE);
		}
		// 数据插入操作循环
		writableDatabase.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		writableDatabase.endTransaction(); // 处理完成
	}

	private ContentValues buildInfo(EducationInfo info) {
		ContentValues values = new ContentValues();
		values.put(EducationInfo.SERVER_ID, info.getServer_id());
		values.put(EducationInfo.TIMESTAMP, info.getTimestamp());
		values.put(EducationInfo.PUBLISHER, info.getPublisher());
		values.put(EducationInfo.COMMENTS, info.getComments());
		values.put(EducationInfo.EMOTION, info.getEmotion());
		values.put(EducationInfo.DINING, info.getDining());
		values.put(EducationInfo.REST, info.getRest());
		values.put(EducationInfo.ACTIVITY, info.getActivity());
		values.put(EducationInfo.EXERCISE, info.getExercise());
		values.put(EducationInfo.SELF_CARE, info.getSelf_care());
		values.put(EducationInfo.MANNER, info.getManner());
		values.put(EducationInfo.GAME, info.getGame());
		values.put(EducationInfo.CHILD_ID, info.getChild_id());
		return values;
	}
	
	List<EducationInfo> getEduRecordByChildID(String childid) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ SqliteHelper.EDUCATION_TAB + " WHERE "
				+ EducationInfo.CHILD_ID + " = '" + childid + "' ORDER BY "
				+ EducationInfo.TIMESTAMP + " DESC", null);

		return getEduList(cursor);
	}

	void removeEduRecord(String childid) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.EDUCATION_TAB + " WHERE "
				+ EducationInfo.CHILD_ID + " = '" + childid + "'");
	}

	private List<EducationInfo> getEduList(Cursor cursor) {
		List<EducationInfo> list = new ArrayList<EducationInfo>();
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				EducationInfo info = getEduByCursor(cursor);
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

	private EducationInfo getEduByCursor(Cursor cursor) {
		EducationInfo info = new EducationInfo();

		info.setId(cursor.getInt(0));
		info.setServer_id(cursor.getInt(1));
		info.setTimestamp(cursor.getLong(2));
		info.setPublisher(cursor.getString(3));
		info.setComments(cursor.getString(4));
		info.setEmotion(cursor.getInt(5));
		info.setDining(cursor.getInt(6));
		info.setRest(cursor.getInt(7));
		info.setActivity(cursor.getInt(8));
		info.setExercise(cursor.getInt(9));
		info.setSelf_care(cursor.getInt(10));
		info.setManner(cursor.getInt(11));
		info.setGame(cursor.getInt(12));
		info.setChild_id(cursor.getString(13));
		return info;
	}
}
