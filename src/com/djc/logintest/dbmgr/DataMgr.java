package com.djc.logintest.dbmgr;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.dbmgr.info.BindedNumInfo;
import com.djc.logintest.dbmgr.info.ChildInfo;
import com.djc.logintest.dbmgr.info.CookBookInfo;
import com.djc.logintest.dbmgr.info.LocationInfo;
import com.djc.logintest.dbmgr.info.Notice;
import com.djc.logintest.dbmgr.info.ScheduleInfo;
import com.djc.logintest.dbmgr.info.SchoolInfo;

public class DataMgr {
	private static int DB_VERSION = 2;
	private static final String DB_NAME = "coolbao" + ".db";
	private static Object mLock = new Object();
	private static DataMgr instance;

	private Context context;
	private SqliteHelper dbHelper;

	private ChildrenInfoMgr childrenInfoMgr;
	private SchoolInfoMgr schoolInfoMgr;
	private ScheduleInfoMgr scheduleInfoMgr;
	private CookBookInfoMgr cookBookInfoMgr;

	public static synchronized DataMgr getInstance() {
		synchronized (mLock) {
			if (instance == null) {
				Log.d("test db 111", "get new instance!");
				instance = new DataMgr();
			}
		}
		return instance;
	}

	public DataMgr() {
		context = MyApplication.getInstance().getApplicationContext();
		dbHelper = new SqliteHelper(context, DB_NAME, null, DB_VERSION);
		childrenInfoMgr = new ChildrenInfoMgr(dbHelper);
		schoolInfoMgr = new SchoolInfoMgr(dbHelper);
		scheduleInfoMgr = new ScheduleInfoMgr(dbHelper);
		cookBookInfoMgr = new CookBookInfoMgr(dbHelper);
	}

	public long addLocationInfo(LocationInfo info) {
		ContentValues values = new ContentValues();
		values.put(LocationInfo.LATITUDE, info.getLatitude());
		values.put(LocationInfo.LONGITUDE, info.getLongitude());
		values.put(LocationInfo.TIMESTAMP, info.getTimestamp());
		values.put(LocationInfo.ADDRESS, info.getAddress());
		values.put(LocationInfo.LBS_NUM, info.getLbs_num());
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		return writableDatabase.insert(SqliteHelper.LOCATION_TAB, null, values);
	}

	public void deleteLocationInfo(int id) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.LOCATION_TAB + " WHERE "
				+ LocationInfo.ID + " = " + id);
	}

	public List<LocationInfo> getLocationInfos() {
		List<LocationInfo> list = new ArrayList<LocationInfo>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ SqliteHelper.LOCATION_TAB + " ORDER BY "
				+ LocationInfo.TIMESTAMP + " DESC", null);

		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				LocationInfo locationInfo = getLocationInfoByCursor(cursor);
				list.add(locationInfo);
				cursor.moveToNext();
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	private LocationInfo getLocationInfoByCursor(Cursor cursor) {
		LocationInfo info = new LocationInfo();

		info.setId(cursor.getInt(0));
		info.setLatitude(cursor.getString(1));
		info.setLongitude(cursor.getString(2));
		info.setTimestamp(cursor.getString(3));
		info.setAddress(cursor.getString(4));
		info.setLbs_num(cursor.getString(5));
		return info;
	}

	public long addBindedNumInfo(BindedNumInfo info) {
		ContentValues values = new ContentValues();
		values.put(BindedNumInfo.PHONE_NUM, info.getPhone_num());
		values.put(BindedNumInfo.NICKNAME, info.getNickname());
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		return writableDatabase.insert(SqliteHelper.BINDED_NUM_TAB, null,
				values);
	}

	public BindedNumInfo getBindedNumInfoByNum(String num) {
		BindedNumInfo info = null;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ SqliteHelper.BINDED_NUM_TAB + " WHERE "
				+ BindedNumInfo.PHONE_NUM + " = '" + num + "'", null);
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				info = getBindedNumInfoByCursor(cursor);
				cursor.moveToNext();
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return info;
	}

	public List<BindedNumInfo> getAllBindedNumInfo() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM "
				+ SqliteHelper.BINDED_NUM_TAB, null);
		return getBindedNumInfoList(cursor);
	}

	public void updateBindedNumInfo(BindedNumInfo info) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(BindedNumInfo.PHONE_NUM, info.getPhone_num());
		values.put(BindedNumInfo.NICKNAME, info.getNickname());
		db.update(SqliteHelper.BINDED_NUM_TAB, values,
				" _id = " + info.getId(), null);
	}

	public void deleteBindedNumInfo(int id) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.BINDED_NUM_TAB + " WHERE "
				+ BindedNumInfo.ID + " = " + id);
	}

	private List<BindedNumInfo> getBindedNumInfoList(Cursor cursor) {
		List<BindedNumInfo> list = new ArrayList<BindedNumInfo>();
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				BindedNumInfo info = getBindedNumInfoByCursor(cursor);
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

	private BindedNumInfo getBindedNumInfoByCursor(Cursor cursor) {
		BindedNumInfo info = new BindedNumInfo();

		info.setId(cursor.getInt(0));
		info.setPhone_num(cursor.getString(1));
		info.setNickname(cursor.getString(2));
		return info;
	}

	public long addNotice(Notice info) {
		ContentValues values = new ContentValues();
		values.put(Notice.TITLE, info.getTitle());
		values.put(Notice.CONTENT, info.getContent());
		values.put(Notice.TIMESTAMP, info.getTimestamp());
		values.put(Notice.NOTICE_TYPE, info.getType());
		values.put(Notice.PUBLISHER, info.getPublisher());
		values.put(Notice.READ, info.getRead());
		values.put(Notice.CHILD_ID, info.getChild_id());
		SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
		return writableDatabase.insert(SqliteHelper.NOTICE_TAB, null, values);
	}

	public Notice getNoticeByID(int id) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.NOTICE_TAB
				+ " WHERE " + Notice.ID + " = " + id, null);
		List<Notice> noticeList = getNoticeList(cursor);
		return noticeList.isEmpty() ? null : noticeList.get(0);
	}

	public List<Notice> getAllNotice() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.NOTICE_TAB
				+ " ORDER BY " + Notice.TIMESTAMP + " DESC", null);

		return getNoticeList(cursor);
	}

	public List<Notice> getAllNoticeByType(int type) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.NOTICE_TAB
				+ " WHERE " + Notice.NOTICE_TYPE + " = " + type + " ORDER BY "
				+ Notice.TIMESTAMP + " DESC", null);

		return getNoticeList(cursor);
	}

	public List<Notice> getNoticeByType(int type, int max) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.NOTICE_TAB
				+ " WHERE " + Notice.NOTICE_TYPE + " = " + type + " ORDER BY "
				+ Notice.TIMESTAMP + " DESC LIMIT " + max, null);

		return getNoticeList(cursor);
	}

	// 获取某天的，全部刷卡记录，其中date的格式必须满足yyyy-mm-dd形式
	// 且只能显示当前选中孩子的信息
	public List<Notice> getAllSwipeCardNotice(String date) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.NOTICE_TAB
				+ " WHERE (" + Notice.NOTICE_TYPE + " = "
				+ JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKIN + " OR "
				+ Notice.NOTICE_TYPE + " = "
				+ JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKOUT + ") AND "
				+ Notice.CHILD_ID + " = '"
				+ childrenInfoMgr.getSelectedChild().getServer_id()
				+ "' AND DATE(" + Notice.TIMESTAMP + ") = '" + date
				+ "' ORDER BY " + Notice.TIMESTAMP + " DESC", null);

		return getNoticeList(cursor);
	}

	// 获取某天的，最晚的刷卡入园记录，如果没有返回""
	// 且只能显示当前选中孩子的信息
	public String getLastestSwipeInNotice(String date) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String sql = "SELECT * FROM " + SqliteHelper.NOTICE_TAB + " WHERE "
				+ Notice.NOTICE_TYPE + " = "
				+ JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKIN + " AND "
				+ Notice.CHILD_ID + " = '"
				+ childrenInfoMgr.getSelectedChild().getServer_id()
				+ "' AND DATE(" + Notice.TIMESTAMP + ") = '" + date
				+ "' ORDER BY " + Notice.TIMESTAMP + " DESC LIMIT 1";
		Cursor cursor = db.rawQuery(sql, null);

		List<Notice> noticeList = getNoticeList(cursor);
		return noticeList.isEmpty() ? "" : noticeList.get(0).getTimestamp();
	}

	// 获取某天的，最晚的刷卡离园记录，如果没有返回""
	// 且只能显示当前选中孩子的信息
	public String getLatestSwipeOutNotice(String date) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.NOTICE_TAB
				+ " WHERE " + Notice.NOTICE_TYPE + " = "
				+ JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKOUT + " AND "
				+ Notice.CHILD_ID + " = '"
				+ childrenInfoMgr.getSelectedChild().getServer_id()
				+ "' AND DATE(" + Notice.TIMESTAMP + ") = '" + date
				+ "' ORDER BY " + Notice.TIMESTAMP + " DESC LIMIT 1", null);

		List<Notice> noticeList = getNoticeList(cursor);
		return noticeList.isEmpty() ? "" : noticeList.get(0).getTimestamp();
	}

	public void deleteNotice(int id) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.NOTICE_TAB + " WHERE "
				+ Notice.ID + " = " + id);
	}

	private List<Notice> getNoticeList(Cursor cursor) {
		List<Notice> list = new ArrayList<Notice>();
		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
				Notice info = getNoticeByCursor(cursor);
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

	private Notice getNoticeByCursor(Cursor cursor) {
		Notice info = new Notice();

		info.setId(cursor.getInt(0));
		info.setTitle(cursor.getString(1));
		info.setContent(cursor.getString(2));
		info.setTimestamp(cursor.getString(3));
		info.setType(cursor.getInt(4));
		info.setPublisher(cursor.getString(5));
		info.setRead(cursor.getInt(6));
		info.setChild_id(cursor.getString(7));
		return info;
	}

	public void upgradeAll() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		dbHelper.clearAll(db);
	}

	public void removeAllNoticeByType(int type) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.NOTICE_TAB + " WHERE "
				+ Notice.NOTICE_TYPE + " = " + type);
	}

	public void removeAllSwipCardNotice() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.NOTICE_TAB + " WHERE "
				+ Notice.NOTICE_TYPE + " = "
				+ JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKIN);
	}

	public void removeAllOtherNotice() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.NOTICE_TAB + " WHERE "
				+ Notice.NOTICE_TYPE + " = " + JSONConstant.NOTICE_TYPE_OTHER);
	}

	// 简单起见，每次更新小孩数据，都是全部删除后插入，因为小孩数量不会很多，大部分只有一个
	// 这样做问题不大
	public void addChildrenInfoList(List<ChildInfo> list) {
		// clearChildrenInfoTable();
		childrenInfoMgr.addChildrenInfoList(list);
	}

	public ChildInfo getSelectedChild() {
		return childrenInfoMgr.getSelectedChild();
	}

	public ChildInfo getChildByID(String id) {
		return childrenInfoMgr.getSelectedChildByID(id);
	}

	public List<ChildInfo> getAllChildrenInfo() {
		return childrenInfoMgr.getAllChildrenInfo();
	}

	public void setSelectedChild(int childid) {
		childrenInfoMgr.setSelectedChild(childid);
	}

	public void updateChildInfo(String serverid, ChildInfo info) {
		childrenInfoMgr.updateChildInfo(serverid, info);
	}

	public void updateChildLocalIconUrl(String serverid, String localurl) {
		childrenInfoMgr.updateLocalUrl(serverid, localurl);
	}

	public void updateNick(String serverid, String nick) {
		childrenInfoMgr.updateNick(serverid, nick);
	}

	public void updateBirthday(String serverid, long birthday) {
		childrenInfoMgr.updateBirthday(serverid, birthday);
	}

	public void updateSchoolInfo(String schoolid, SchoolInfo info) {
		schoolInfoMgr.updateSchoolInfo(schoolid, info);
	}

	public long addSchoolInfo(SchoolInfo info) {
		return schoolInfoMgr.addSchoolInfo(info);
	}

	public SchoolInfo getSchoolInfo() {
		return schoolInfoMgr.getSchoolInfo();
	}

	public String getSchoolID() {
		return schoolInfoMgr.getSchoolInfo().getSchool_id();
	}

	public void updateSchoolLogoLocalUrl(String schoolid, String localurl) {
		schoolInfoMgr.updateSchoolLogoLocalUrl(schoolid, localurl);
	}

	public long addScheduleInfo(ScheduleInfo info) {
		return scheduleInfoMgr.addScheduleInfo(info);
	}

	public void updateScheduleInfo(ScheduleInfo info) {
		scheduleInfoMgr.updateScheduleInfo(info);
	}

	public ScheduleInfo getScheduleInfo() {
		return scheduleInfoMgr.getScheduleInfo();
	}

	public void updateCookBookInfo(CookBookInfo info) {
		cookBookInfoMgr.updateCookBookInfo(info);
	}

	public CookBookInfo getCookBookInfo() {
		return cookBookInfoMgr.getCookBookInfo();
	}

	public void clearSchoolInfoTable() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.SCHOOL_INFO_TAB);
	}

	public void clearChildrenInfoTable() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.CHILDREN_INFO_TAB);
	}

	public void clearNoticeTable() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.NOTICE_TAB);
	}

	public void clearLocationTable() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.LOCATION_TAB);
	}

	public void close() {
		synchronized (mLock) {
			if (dbHelper != null) {
				dbHelper.close();
				dbHelper = null;
			}

			instance = null;
		}
	}

}
