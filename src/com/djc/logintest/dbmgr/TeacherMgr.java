package com.djc.logintest.dbmgr;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.djc.logintest.dbmgr.info.Teacher;

public class TeacherMgr {
	private SqliteHelper dbHelper;

	public TeacherMgr(SqliteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	long add(Teacher info) {
		ContentValues values = setProp(info);
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		return writableDatabase.insertWithOnConflict(SqliteHelper.TEACHER_TAB, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	void addList(List<Teacher> list) {
		if (list == null || list.isEmpty()) {
			return;
		}

		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		writableDatabase.beginTransaction(); // 手动设置开始事务

		for (Teacher info : list) {
			ContentValues values = setProp(info);
			writableDatabase.insertWithOnConflict(SqliteHelper.TEACHER_TAB, null, values,
					SQLiteDatabase.CONFLICT_REPLACE);
		}

		// 数据插入操作循环
		writableDatabase.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
		writableDatabase.endTransaction(); // 处理完成
	}

	private ContentValues setProp(Teacher info) {
		ContentValues values = new ContentValues();
		values.put(Teacher.BIRTHDAY, info.getBirthday());
		values.put(Teacher.GENDER, info.getGender());
		values.put(Teacher.HEAD_ICON, info.getHead_icon());
		values.put(Teacher.NAME, info.getName());
		values.put(Teacher.PHONE, info.getPhone());
		values.put(Teacher.SERVER_ID, info.getServer_id());
		values.put(Teacher.SHOOL_ID, info.getShool_id());
		values.put(Teacher.TIMESTAMP, info.getTimestamp());
		values.put(Teacher.WORKDUTY, info.getWorkduty());
		values.put(Teacher.WORKGROUP, info.getWorkgroup());
		return values;
	}

	// 处理从服务器端获取到的教师信息，如果客户端没有，则保存，如果有，并且服务器端信息更新，则更新
	boolean handleIncomingTeacher(Teacher fromnet) {
		boolean bret = false;
		Teacher localone = getTeacher(fromnet.getPhone());
		if (localone == null) {
			add(fromnet);
			return true;
		} else if (fromnet.getTimestamp() > localone.getTimestamp()) {
			add(fromnet);
		}

		return bret;
	}

	boolean exist(String phone) {
		return getTeacher(phone) != null;
	}

	Teacher getTeacher(String phone) {
		Teacher info = null;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.TEACHER_TAB + " WHERE " + Teacher.PHONE + " ='"
				+ phone + "'", null);
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				info = getInfoByCursor(cursor);
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

	List<Teacher> getAllTeachers() {
		List<Teacher> list = new ArrayList<Teacher>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.TEACHER_TAB, null);
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				Teacher info = getInfoByCursor(cursor);
				cursor.moveToNext();
				list.add(info);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return list;
	}

	void removeAllTeacher() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.TEACHER_TAB);
	}
	
	private Teacher getInfoByCursor(Cursor cursor) {
		Teacher info = new Teacher();

		info.setId(cursor.getInt(0));
		info.setName(cursor.getString(1));
		info.setHead_icon(cursor.getString(2));
		info.setTimestamp(cursor.getLong(3));
		info.setBirthday(cursor.getString(4));
		info.setServer_id(cursor.getString(5));
		info.setWorkgroup(cursor.getString(6));
		info.setWorkduty(cursor.getString(7));
		info.setGender(cursor.getInt(8));
		info.setShool_id(cursor.getInt(9));
		info.setPhone(cursor.getString(10));
		return info;
	}
	
	
}
