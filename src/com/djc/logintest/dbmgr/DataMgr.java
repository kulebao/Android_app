package com.djc.logintest.dbmgr;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.dbmgr.info.BindedNumInfo;
import com.djc.logintest.dbmgr.info.ChatInfo;
import com.djc.logintest.dbmgr.info.ChildInfo;
import com.djc.logintest.dbmgr.info.CookBookInfo;
import com.djc.logintest.dbmgr.info.EducationInfo;
import com.djc.logintest.dbmgr.info.Homework;
import com.djc.logintest.dbmgr.info.LocationInfo;
import com.djc.logintest.dbmgr.info.News;
import com.djc.logintest.dbmgr.info.ScheduleInfo;
import com.djc.logintest.dbmgr.info.SchoolInfo;
import com.djc.logintest.dbmgr.info.SwipeInfo;

public class DataMgr {
	private static int DB_VERSION = 1;
	private static final String DB_NAME = "coolbao" + ".db";
	private static Object mLock = new Object();
	private static DataMgr instance;

	private Context context;
	private SqliteHelper dbHelper;

	private ChildrenInfoMgr childrenInfoMgr;
	private SchoolInfoMgr schoolInfoMgr;
	private ScheduleInfoMgr scheduleInfoMgr;
	private CookBookInfoMgr cookBookInfoMgr;
	private NewsMgr newsMgr;
	private SwipeMgr swipeMgr;
	private HomeworkMgr homeworkMgr;
	private ChatMgr chatMgr;
	private EducationMgr educationMgr;

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
		newsMgr = new NewsMgr(dbHelper);
		swipeMgr = new SwipeMgr(dbHelper);
		homeworkMgr = new HomeworkMgr(dbHelper);
		chatMgr = new ChatMgr(dbHelper);
		educationMgr = new EducationMgr(dbHelper);
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

	public void upgradeAll() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		dbHelper.clearAll(db);
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

	public long addNews(News info) {
		return newsMgr.addNews(info);
	}

	public void addNewsList(List<News> list) {
		newsMgr.addNewsList(list);
	}

	public News getNewsByID(int id) {
		return newsMgr.getNewsByID(id);
	}

	public List<News> getAllNewsByType(int type) {
		return newsMgr.getAllNewsByType(type);
	}

	public List<News> getNewsByType(int type, int max) {
		return newsMgr.getNewsByType(type, max);
	}

	public void removeAllNewsByType(int type) {
		newsMgr.removeAllNewsByType(type);
	}

	public long addSwipeData(SwipeInfo info) {
		return swipeMgr.addData(info);
	}

	public void addSwipeDataList(List<SwipeInfo> list) {
		swipeMgr.addDataList(list);
	}

	public SwipeInfo getSwipeDataByID(int id) {
		return swipeMgr.getDataByID(id);
	}

	public SwipeInfo getSwipeDataByTimeStamp(long timestamp) {
		return swipeMgr.getDataByTimeStamp(timestamp);
	}

	public String getLastestSwipeIn(String date) {
		return swipeMgr.getLastestSwipeIn(date, childrenInfoMgr
				.getSelectedChild().getServer_id());
	}

	public String getLatestSwipeOut(String date) {
		return swipeMgr.getLatestSwipeOut(date, childrenInfoMgr
				.getSelectedChild().getServer_id());
	}

	public List<SwipeInfo> getAllSwipeCardNotice(String date) {
		return swipeMgr.getAllSwipeCardNotice(date, childrenInfoMgr
				.getSelectedChild().getServer_id());
	}

	public long addHomework(Homework info) {
		return homeworkMgr.addHomework(info);
	}

	public void addHomeworkList(List<Homework> list) {
		homeworkMgr.addHomeworkList(list);
	}

	public Homework getHomeworkByID(int id) {
		return homeworkMgr.getHomeworkByID(id);
	}

	public List<Homework> getHomeworkWithLimite(int max) {
		return homeworkMgr.getHomeworkWithLimite(max);
	}

	public void removeAllHomework() {
		homeworkMgr.removeAllHomework();
	}

	public List<ChatInfo> getChatInfoWithLimite(int max) {
		return chatMgr.getChatInfoWithLimite(max);
	}

	public List<ChatInfo> getChatInfoWithLimite(int max, long to) {
		return chatMgr.getChatInfoWithLimite(max, to);
	}

	public void removeAllChatInfo() {
		chatMgr.removeAllChatInfo();
	}

	public void addChatInfoList(List<ChatInfo> list) {
		chatMgr.addChatInfoList(list);
	}

	public int getLastChatServerid() {
		return chatMgr.getLastServerid();
	}

	public void addEduRecordList(List<EducationInfo> list) {
		educationMgr.addEduRecordList(list);
	}

	public List<EducationInfo> getEduRecordByChildID(String childid) {
		return educationMgr.getEduRecordByChildID(childid);
	}

	public void removeEduRecord(String childid) {
		educationMgr.removeEduRecord(childid);
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
