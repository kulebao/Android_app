package com.cocobabys.dbmgr;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cocobabys.activities.MyApplication;
import com.cocobabys.bean.GroupExpInfo;
import com.cocobabys.dbmgr.info.BindedNumInfo;
import com.cocobabys.dbmgr.info.ChatInfo;
import com.cocobabys.dbmgr.info.ChildInfo;
import com.cocobabys.dbmgr.info.CookBookInfo;
import com.cocobabys.dbmgr.info.EducationInfo;
import com.cocobabys.dbmgr.info.ExpInfo;
import com.cocobabys.dbmgr.info.Homework;
import com.cocobabys.dbmgr.info.LocationInfo;
import com.cocobabys.dbmgr.info.NewChatInfo;
import com.cocobabys.dbmgr.info.News;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.dbmgr.info.ScheduleInfo;
import com.cocobabys.dbmgr.info.SchoolInfo;
import com.cocobabys.dbmgr.info.SwipeInfo;
import com.cocobabys.dbmgr.info.Teacher;
import com.cocobabys.utils.Utils;

public class DataMgr {
	// 1 -> 2
	// 1)增加一个家长表 parent_tab
	// 2）TEACHER_TAB 修改了unique，之前是phone 改为 teacherid
	// ********************
	// 2 -> 3
	// 新增 EXP_TAB, MONTH_TAB
	// CHILDREN_INFO_TAB 增加小孩id为unique

	private static int DB_VERSION = 3;
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
	private TeacherMgr teacherMgr;
	private ParentMgr parentMgr;
	private NewChatMgr newChatMgr;
	private ExpMgr expMgr;

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
		teacherMgr = new TeacherMgr(dbHelper);
		parentMgr = new ParentMgr(dbHelper);
		newChatMgr = new NewChatMgr(dbHelper);
		expMgr = new ExpMgr(dbHelper);
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
		db.execSQL("DELETE FROM " + SqliteHelper.LOCATION_TAB + " WHERE " + LocationInfo.ID + " = " + id);
	}

	public List<LocationInfo> getLocationInfos() {
		List<LocationInfo> list = new ArrayList<LocationInfo>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.LOCATION_TAB + " ORDER BY "
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
		return writableDatabase.insert(SqliteHelper.BINDED_NUM_TAB, null, values);
	}

	public BindedNumInfo getBindedNumInfoByNum(String num) {
		BindedNumInfo info = null;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.BINDED_NUM_TAB + " WHERE "
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
		Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.BINDED_NUM_TAB, null);
		return getBindedNumInfoList(cursor);
	}

	public void updateBindedNumInfo(BindedNumInfo info) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(BindedNumInfo.PHONE_NUM, info.getPhone_num());
		values.put(BindedNumInfo.NICKNAME, info.getNickname());
		db.update(SqliteHelper.BINDED_NUM_TAB, values, " _id = " + info.getId(), null);
	}

	public void deleteBindedNumInfo(int id) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + SqliteHelper.BINDED_NUM_TAB + " WHERE " + BindedNumInfo.ID + " = " + id);
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

	public void clearChildInfo() {
		childrenInfoMgr.clearChildInfo();
	}

	public ChildInfo getSelectedChild() {
		return childrenInfoMgr.getSelectedChild();
	}

	public ChildInfo getChildByID(String id) {
		return childrenInfoMgr.getChildByID(id);
	}

	public List<ChildInfo> getAllChildrenInfo() {
		return childrenInfoMgr.getAllChildrenInfo();
	}

	public List<String> getAllClassID() {
		return childrenInfoMgr.getAllClassID();
	}

	public int setSelectedChild(String childid) {
		return childrenInfoMgr.setSelectedChild(childid);
	}

	public String getLatestChildTimestamp() {
		return childrenInfoMgr.getLatestTimestamp();
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

	public String getClassNameByClassID(int classid) {
		return childrenInfoMgr.getClassNameByClassID(classid);
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

	public SwipeInfo getSwipeDataByTimeStamp(long timestamp) {
		return swipeMgr.getDataByTimeStamp(timestamp);
	}

	public String getLastestSwipeIn(String date) {
		return swipeMgr.getLastestSwipeIn(date, childrenInfoMgr.getSelectedChild().getServer_id());
	}

	public String getLatestSwipeOut(String date) {
		return swipeMgr.getLatestSwipeOut(date, childrenInfoMgr.getSelectedChild().getServer_id());
	}

	public List<SwipeInfo> getAllSwipeCardNotice(String date) {
		return swipeMgr.getAllSwipeCardNotice(date, childrenInfoMgr.getSelectedChild().getServer_id());
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

	public List<EducationInfo> getSelectedChildEduRecord() {
		return educationMgr.getEduRecordByChildID(childrenInfoMgr.getSelectedChild().getServer_id());
	}

	public void removeSelectedChildEduRecord() {
		educationMgr.removeEduRecord(childrenInfoMgr.getSelectedChild().getServer_id());
	}

	public void removeEduRecord(String childid) {
		educationMgr.removeEduRecord(childid);
	}

	public long addTeacher(Teacher info) {
		return teacherMgr.add(info);
	}

	public List<Teacher> getAllTeachers() {
		return teacherMgr.getAllTeachers();
	}

	public boolean isTeacherExist(String phone) {
		return teacherMgr.exist(phone);
	}

	public void addTeacherList(List<Teacher> list) {
		teacherMgr.addList(list);
	}

	public boolean handleIncomingTeacher(Teacher fromnet) {
		return teacherMgr.handleIncomingTeacher(fromnet);
	}

	public Teacher getTeacher(String phone) {
		return teacherMgr.getTeacher(phone);
	}

	public Teacher getTeacherByID(String teacherid) {
		return teacherMgr.getTeacherByID(teacherid);
	}

	public void removeAllTeacher() {
		teacherMgr.removeAllTeacher();
	}

	public void addParentList(List<ParentInfo> list) {
		parentMgr.addDataList(list);
	}

	public ParentInfo getParentByPhone(String phone) {
		return parentMgr.getParentByPhone(phone);
	}

	public ParentInfo getParentByID(String parentid) {
		return parentMgr.getParentByID(parentid);
	}

	// 获取当前家长信息
	public ParentInfo getSelfInfoByPhone() {
		return parentMgr.getParentByPhone(Utils.getAccount());
	}

	public List<NewChatInfo> getNewChatInfoWithLimite(int max, String childid) {
		return newChatMgr.getChatInfoWithLimite(max, childid);
	}

	public List<NewChatInfo> getNewChatInfoWithLimite(int max, long to, String childid) {
		return newChatMgr.getChatInfoWithLimite(max, to, childid);
	}

	public void addNewChatInfoList(List<NewChatInfo> list) {
		newChatMgr.addDataList(list);
	}

	public void removeAllNewChatInfo() {
		newChatMgr.clear();
	}

	public long getLastNewChatServerid(String childid) {
		NewChatInfo lastChatInfo = newChatMgr.getLastChatInfo(childid);
		if (lastChatInfo != null) {
			return lastChatInfo.getChat_id();
		}
		return -1;
	}

	public void addExpDataList(List<ExpInfo> list) {
		expMgr.addDataList(list);
	}

	public List<GroupExpInfo> getExpCountGroupByMonthPerYear(int year) {
		String childid = getSelectedChild().getServer_id();
		return expMgr.getExpCountGroupByMonthPerYear(year, childid);
	}

	public int getExpCountInMonth(int year, String month) {
		String childid = getSelectedChild().getServer_id();
		return expMgr.getExpCountInMonth(year, month, childid);
	}

	public List<ExpInfo> getExpInfoByMonthAndYear(String monthAndYear) {
		return expMgr.getExpInfoByMonthAndYear(monthAndYear);
	}

	public ExpInfo getExpInfoByID(long expid) {
		return expMgr.getExpInfoByID(expid);
	}

	public void clearExp() {
		expMgr.clear();
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