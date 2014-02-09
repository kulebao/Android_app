package com.djc.logintest.dbmgr;

import com.djc.logintest.dbmgr.info.BindedNumInfo;
import com.djc.logintest.dbmgr.info.ChildInfo;
import com.djc.logintest.dbmgr.info.CookBookInfo;
import com.djc.logintest.dbmgr.info.InfoHelper;
import com.djc.logintest.dbmgr.info.LocationInfo;
import com.djc.logintest.dbmgr.info.News;
import com.djc.logintest.dbmgr.info.Notice;
import com.djc.logintest.dbmgr.info.ScheduleInfo;
import com.djc.logintest.dbmgr.info.SchoolInfo;
import com.djc.logintest.dbmgr.info.SwipeInfo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SqliteHelper extends SQLiteOpenHelper {
    public static final String NOTICE_TAB = "notice_tab";
    public static final String LOCATION_TAB = "location_tab";
    public static final String BINDED_NUM_TAB = "binded_num_tab";
    public static final String CHILDREN_INFO_TAB = "children_info_tab";
    public static final String SCHOOL_INFO_TAB = "school_info_tab";
    public static final String SCHEDULE_INFO_TAB = "schedule_info_tab";
    public static final String COOKBOOK_INFO_TAB = "cookbook_info_tab";
    public static final String NEWS_TAB = "news_tab";
    public static final String SWIPE_TAB = "swipe_tab";

    public SqliteHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + NOTICE_TAB + "(" + Notice.ID
                + " integer primary key autoincrement," + Notice.TITLE + " varchar,"
                + Notice.CONTENT + " varchar," + Notice.TIMESTAMP + " timestamp ,"
                + Notice.NOTICE_TYPE + " integer," + Notice.PUBLISHER + " varchar," + Notice.READ
                + " integer," + Notice.CHILD_ID + " varchar" + ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + LOCATION_TAB + "(" + LocationInfo.ID
                + " integer primary key autoincrement," + LocationInfo.LATITUDE + " varchar,"
                + LocationInfo.LONGITUDE + " varchar," + LocationInfo.TIMESTAMP + " timestamp, "
                + LocationInfo.ADDRESS + " varchar, " + LocationInfo.LBS_NUM + " varchar " + ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + BINDED_NUM_TAB + "(" + BindedNumInfo.ID
                + " integer primary key autoincrement," + BindedNumInfo.PHONE_NUM + " varchar,"
                + BindedNumInfo.NICKNAME + " varchar" + ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + CHILDREN_INFO_TAB + "(" + ChildInfo.ID
                + " integer primary key autoincrement," + ChildInfo.CHILD_NICK_NAME + " varchar,"
                + ChildInfo.CHILD_LOCAL_HEAD_ICON + " varchar," + ChildInfo.CHILD_SERVER_HEAD_ICON
                + " varchar," + ChildInfo.CHILD_BIRTHDAY + " varchar," + ChildInfo.SELECTED
                + " integer," + InfoHelper.TIMESTAMP + " varchar," + ChildInfo.SERVER_ID
                + " varchar," + ChildInfo.CLASS_ID + " varchar," + ChildInfo.CLASS_NAME
                + " varchar" + ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + SCHOOL_INFO_TAB + "(" + SchoolInfo.ID
                + " integer primary key autoincrement," + SchoolInfo.SCHOOL_ID + " varchar,"
                + SchoolInfo.SCHOOL_NAME + " varchar," + SchoolInfo.SCHOOL_PHONE + " varchar,"
                + SchoolInfo.SCHOOL_DESC + " varchar," + SchoolInfo.SCHOOL_LOCAL_URL + " integer,"
                + SchoolInfo.SCHOOL_SERVER_URL + " varchar," + InfoHelper.TIMESTAMP + " varchar"
                + ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + SCHEDULE_INFO_TAB + "(" + ScheduleInfo.ID
                + " integer primary key autoincrement," + ScheduleInfo.SCHEDULE_ID + " varchar,"
                + ScheduleInfo.SCHEDULE_CONTENT + " varchar," + InfoHelper.TIMESTAMP + " varchar"
                + ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + COOKBOOK_INFO_TAB + "(" + CookBookInfo.ID
                + " integer primary key autoincrement," + CookBookInfo.COOKBOOK_ID + " varchar,"
                + CookBookInfo.COOKBOOK_CONTENT + " varchar," + InfoHelper.TIMESTAMP + " varchar"
                + ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + NEWS_TAB + "(" + News.ID
                + " integer primary key autoincrement," + News.TITLE + " varchar," + News.CONTENT
                + " varchar," + News.TIMESTAMP + " biginteger ," + News.NEWS_TYPE + " integer,"
                + News.NEWS_SERVER_ID + " integer," + News.PUBLISHER + " varchar" + ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + SWIPE_TAB + "(" + SwipeInfo.ID
                + " integer primary key autoincrement," + SwipeInfo.TIMESTAMP + " biginteger ,"
                + SwipeInfo.TYPE + " integer," + SwipeInfo.CHILD_ID + " varchar,"
                + SwipeInfo.ICON_URL + " varchar," + "UNIQUE(" + SwipeInfo.TIMESTAMP + ") " + ")");

        // 对于用到联合查询的字段，需要增加索引，否则效率很低
        // db.execSQL("create index propindex on propertytab(property_value)");
        // db.execSQL("create index rindex on contacttab(rosterid)");
        // db.execSQL("create index ismashupedindex on contacttab(ismashuped)");
        Log.d("Database", "onCreate");
    }

    @Override
    // 当数据库升级时，可以根据oldVersion，newVersion来做一些数据迁移，更新的操作
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("Database", "onUpgrade");
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL("ALTER TABLE children_info_tab ADD COLUMN class_name varchar;");
        }
    }

    public void clearAll(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + NOTICE_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + BINDED_NUM_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + CHILDREN_INFO_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + SCHOOL_INFO_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + SCHEDULE_INFO_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + COOKBOOK_INFO_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + NEWS_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + SWIPE_TAB);
        onCreate(db);
    }
}
