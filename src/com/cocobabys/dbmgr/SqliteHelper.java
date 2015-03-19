package com.cocobabys.dbmgr;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.cocobabys.activities.MyApplication;
import com.cocobabys.dbmgr.info.ChildInfo;
import com.cocobabys.dbmgr.info.CookBookInfo;
import com.cocobabys.dbmgr.info.EducationInfo;
import com.cocobabys.dbmgr.info.ExpInfo;
import com.cocobabys.dbmgr.info.Homework;
import com.cocobabys.dbmgr.info.InfoHelper;
import com.cocobabys.dbmgr.info.NativeMediumInfo;
import com.cocobabys.dbmgr.info.NewChatInfo;
import com.cocobabys.dbmgr.info.News;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.dbmgr.info.ReceiptInfo;
import com.cocobabys.dbmgr.info.ScheduleInfo;
import com.cocobabys.dbmgr.info.SchoolInfo;
import com.cocobabys.dbmgr.info.SwipeInfo;
import com.cocobabys.dbmgr.info.Teacher;

public class SqliteHelper extends SQLiteOpenHelper{
    // public static final String LOCATION_TAB = "location_tab";
    // public static final String BINDED_NUM_TAB = "binded_num_tab";
    // public static final String CHAT_TAB = "chat_tab";
    public static final String CHILDREN_INFO_TAB     = "children_info_tab";
    public static final String SCHOOL_INFO_TAB       = "school_info_tab";
    public static final String SCHEDULE_INFO_TAB     = "schedule_info_tab";
    public static final String COOKBOOK_INFO_TAB     = "cookbook_info_tab";
    public static final String NEWS_TAB              = "news_tab";
    public static final String SWIPE_TAB             = "swipe_tab";
    public static final String HOMEWORK_TAB          = "homework_tab";
    public static final String EDUCATION_TAB         = "education_tab";
    public static final String TEACHER_TAB           = "teacher_tab";
    public static final String PARENT_TAB            = "parent_tab";
    public static final String NEW_CHAT_TAB          = "new_chat_tab";
    public static final String EXP_TAB               = "exp_tab";
    public static final String MONTH_TAB             = "month_tab";
    // 家长自己从本地选择多媒体资源发送，需要保存文件路径，否则又要从服务器上下载
    public static final String NATIVE_MEDIUM_URL_TAB = "native_medium_url_tab";
    public static final String RECEIPT_TAB           = "receipt_tab";

    public SqliteHelper(Context context, String name, CursorFactory factory, int version){
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db){

        addTabs(db);

        // 对于用到联合查询的字段，需要增加索引，否则效率很低
        // db.execSQL("create index if not exists indexname on tablename(colum1,colum2)");
        // db.execSQL("create index propindex on propertytab(property_value)");
        // db.execSQL("create index rindex on contacttab(rosterid)");
        // db.execSQL("create index ismashupedindex on contacttab(ismashuped)");
        Log.d("Database", "onCreate");
    }

    void initMonthTab(SQLiteDatabase db){
        try{
            db.execSQL("CREATE TABLE IF NOT EXISTS " + MONTH_TAB + "(month_name varchar,  " + " UNIQUE(month_name) "
                    + ") ");
            db.beginTransaction(); // 手动设置开始事务
            for(int i = 1; i < 13; i++){
                String month = "";
                if(i < 10){
                    month = "0" + i;
                } else{
                    month = "" + i;
                }
                ContentValues values = new ContentValues();
                values.put("month_name", month);
                db.insertWithOnConflict(SqliteHelper.MONTH_TAB, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }// 数据插入操作循环
            db.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
        } catch(Exception e){
            e.printStackTrace();
        }
        finally{
            db.endTransaction(); // 处理完成
        }
    }

    void addExpTab(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS " + EXP_TAB + "(" + ExpInfo.ID + " integer primary key autoincrement,"
                + ExpInfo.EXP_ID + " biginteger," + ExpInfo.CHILD_ID + " varchar, " + ExpInfo.CONTENT + " varchar, "
                + ExpInfo.MEDIUM + " varchar, " + ExpInfo.SENDER_ID + " varchar, " + ExpInfo.SENDER_TYPE + " varchar, "
                + ExpInfo.TIMESTAMP + " biginteger, " + " UNIQUE(" + ExpInfo.EXP_ID + ") " + ")");
        try{
            db.execSQL("CREATE INDEX IF NOT EXISTS timeindex on " + EXP_TAB + "(" + ExpInfo.TIMESTAMP + ")");
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    private void addTabs(SQLiteDatabase db){
        // addLocationTab(db);
        // addChatTab(db);
        // addBindedNumTab(db);

        addChildTab(db);

        addSchoolInfoTab(db);

        addScheduleTab(db);

        addCookbookTab(db);

        addNewsTab(db);

        addSwipeTab(db);

        addHomeworkTab(db);

        addEduTab(db);

        addTeacherTab(db);

        addParentTab(db);

        addNewChatTab(db);

        addExpTab(db);

        addNativeMediumUrlTab(db);

        addReceiptTab(db);

        initMonthTab(db);
    }

    // private void addLocationTab(SQLiteDatabase db) {
    // db.execSQL("CREATE TABLE IF NOT EXISTS " + LOCATION_TAB + "("
    // + LocationInfo.ID + " integer primary key autoincrement,"
    // + LocationInfo.LATITUDE + " varchar," + LocationInfo.LONGITUDE
    // + " varchar," + LocationInfo.TIMESTAMP + " timestamp, "
    // + LocationInfo.ADDRESS + " varchar, " + LocationInfo.LBS_NUM
    // + " varchar " + ")");
    // }
    //
    // private void addBindedNumTab(SQLiteDatabase db) {
    // db.execSQL("CREATE TABLE IF NOT EXISTS " + BINDED_NUM_TAB + "("
    // + BindedNumInfo.ID + " integer primary key autoincrement,"
    // + BindedNumInfo.PHONE_NUM + " varchar,"
    // + BindedNumInfo.NICKNAME + " varchar" + ")");
    // }

    void addChildTab(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS " + CHILDREN_INFO_TAB + "(" + ChildInfo.ID
                + " integer primary key autoincrement," + ChildInfo.CHILD_NICK_NAME + " varchar,"
                + ChildInfo.CHILD_LOCAL_HEAD_ICON + " varchar," + ChildInfo.CHILD_SERVER_HEAD_ICON + " varchar,"
                + ChildInfo.CHILD_BIRTHDAY + " biginteger," + ChildInfo.SELECTED + " integer," + InfoHelper.TIMESTAMP
                + " biginteger," + ChildInfo.SERVER_ID + " varchar," + ChildInfo.CLASS_ID + " varchar,"
                + ChildInfo.CLASS_NAME + " varchar," + ChildInfo.CHILD_NAME + " varchar," + ChildInfo.GENDER
                + " integer," + " UNIQUE(" + ChildInfo.SERVER_ID + ")" + ")");

        try{
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idindex on " + CHILDREN_INFO_TAB + "(" + ChildInfo.SERVER_ID
                    + ")");
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    void addSchoolInfoTab(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SCHOOL_INFO_TAB + "(" + SchoolInfo.ID
                + " integer primary key autoincrement," + SchoolInfo.SCHOOL_ID + " varchar," + SchoolInfo.SCHOOL_NAME
                + " varchar," + SchoolInfo.SCHOOL_PHONE + " varchar," + SchoolInfo.SCHOOL_DESC + " varchar,"
                + SchoolInfo.SCHOOL_LOCAL_URL + " integer," + SchoolInfo.SCHOOL_SERVER_URL + " varchar,"
                + InfoHelper.TIMESTAMP + " varchar" + ")");
    }

    void addScheduleTab(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SCHEDULE_INFO_TAB + "(" + ScheduleInfo.ID
                + " integer primary key autoincrement," + ScheduleInfo.SCHEDULE_ID + " varchar,"
                + ScheduleInfo.SCHEDULE_CONTENT + " varchar," + InfoHelper.TIMESTAMP + " varchar" + ")");
    }

    void addCookbookTab(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS " + COOKBOOK_INFO_TAB + "(" + CookBookInfo.ID
                + " integer primary key autoincrement," + CookBookInfo.COOKBOOK_ID + " varchar,"
                + CookBookInfo.COOKBOOK_CONTENT + " varchar," + InfoHelper.TIMESTAMP + " varchar" + ")");
    }

    void addNewsTab(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS " + NEWS_TAB + "(" + News.ID + " integer primary key autoincrement,"
                + News.TITLE + " varchar," + News.CONTENT + " varchar," + News.TIMESTAMP + " biginteger ,"
                + News.NEWS_TYPE + " integer," + News.NEWS_SERVER_ID + " integer," + News.PUBLISHER + " varchar,"
                + News.ICON_URL + " varchar," + News.CLASS_ID + " integer," + News.NEED_RECEIPT + " integer,"
                + "UNIQUE(" + News.NEWS_SERVER_ID + ") " + ")");
    }

    void addSwipeTab(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SWIPE_TAB + "(" + SwipeInfo.ID
                + " integer primary key autoincrement," + SwipeInfo.TIMESTAMP + " biginteger ," + SwipeInfo.TYPE
                + " integer," + SwipeInfo.CHILD_ID + " varchar," + SwipeInfo.ICON_URL + " varchar,"
                + SwipeInfo.PARENT_NAME + " varchar," + "UNIQUE(" + SwipeInfo.TIMESTAMP + ") " + ")");
    }

    void addHomeworkTab(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS " + HOMEWORK_TAB + "(" + Homework.ID
                + " integer primary key autoincrement," + Homework.TITLE + " varchar," + Homework.CONTENT + " varchar,"
                + Homework.TIMESTAMP + " biginteger ," + Homework.ICON_URL + " varchar," + Homework.SERVER_ID
                + " integer," + Homework.CLASS_ID + " integer," + Homework.PUBLISHER + " varchar," + "UNIQUE("
                + Homework.SERVER_ID + ") " + ")");
    }

    void addNativeMediumUrlTab(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS " + NATIVE_MEDIUM_URL_TAB + "(" + NativeMediumInfo.ID
                + " integer primary key autoincrement," + NativeMediumInfo.KEY + " varchar," + NativeMediumInfo.VALUE
                + " varchar," + "UNIQUE(" + NativeMediumInfo.KEY + "," + NativeMediumInfo.VALUE + ") " + ")");
    }

    void addReceiptTab(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS " + RECEIPT_TAB + "(" + ReceiptInfo.ID
                + " integer primary key autoincrement," + ReceiptInfo.RECEIPT_ID + " biginteger,"
                + ReceiptInfo.RECEIPT_STATE + " integer," + "UNIQUE(" + ReceiptInfo.RECEIPT_ID + ") " + ")");
    }

    // private void addChatTab(SQLiteDatabase db) {
    // db.execSQL("CREATE TABLE IF NOT EXISTS " + CHAT_TAB + "(" + ChatInfo.ID
    // + " integer primary key autoincrement," + ChatInfo.SENDER
    // + " varchar," + ChatInfo.CONTENT + " varchar,"
    // + ChatInfo.TIMESTAMP + " biginteger ," + ChatInfo.ICON_URL
    // + " varchar," + ChatInfo.SERVER_ID + " integer,"
    // + ChatInfo.SEND_RESULT + " integer," + ChatInfo.PHONE
    // + " varchar," + "UNIQUE(" + ChatInfo.SERVER_ID + ") " + ")");
    // }

    void addEduTab(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS " + EDUCATION_TAB + "(" + EducationInfo.ID
                + " integer primary key autoincrement," + EducationInfo.SERVER_ID + " integer,"
                + EducationInfo.TIMESTAMP + " biginteger," + EducationInfo.PUBLISHER + " varchar ,"
                + EducationInfo.COMMENTS + " varchar," + EducationInfo.EMOTION + " integer," + EducationInfo.DINING
                + " integer," + EducationInfo.REST + " integer," + EducationInfo.ACTIVITY + " integer,"
                + EducationInfo.EXERCISE + " integer," + EducationInfo.SELF_CARE + " integer," + EducationInfo.MANNER
                + " integer," + EducationInfo.GAME + " integer," + EducationInfo.CHILD_ID + " varchar," + "UNIQUE("
                + EducationInfo.SERVER_ID + ") " + ")");
    }

    void addTeacherTab(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TEACHER_TAB + "(" + Teacher.ID
                + " integer primary key autoincrement," + Teacher.NAME + " varchar," + Teacher.HEAD_ICON + " varchar,"
                + Teacher.TIMESTAMP + " biginteger ," + Teacher.BIRTHDAY + " varchar," + Teacher.SERVER_ID
                + " varchar," + Teacher.WORKGROUP + " varchar," + Teacher.WORKDUTY + " varchar," + Teacher.GENDER
                + " integer," + Teacher.SHOOL_ID + " integer," + Teacher.PHONE + " varchar," + "UNIQUE("
                + Teacher.SERVER_ID + ") " + ")");
    }

    void addNewChatTab(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS " + NEW_CHAT_TAB + "(" + NewChatInfo.ID
                + " integer primary key autoincrement," + NewChatInfo.CHAT_ID + " biginteger," + NewChatInfo.CHILD_ID
                + " varchar, " + NewChatInfo.CONTENT + " varchar, " + NewChatInfo.MEDIA_TYPE + " varchar, "
                + NewChatInfo.MEDIA_URL + " varchar, " + NewChatInfo.SENDER_ID + " varchar, " + NewChatInfo.SENDER_TYPE
                + " varchar, " + NewChatInfo.TIMESTAMP + " biginteger, " + " UNIQUE(" + NewChatInfo.CHAT_ID + ") "
                + ")");
    }

    void addParentTab(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PARENT_TAB + "(" + ParentInfo.ID
                + " integer primary key autoincrement," + ParentInfo.CARD_NUMBER + " varchar,"
                + ParentInfo.MEMBER_STATUS + " integer, " + ParentInfo.PARENT_ID + " varchar, "
                + ParentInfo.PARENT_NAME + " varchar, " + ParentInfo.PHONE + " varchar, " + ParentInfo.PORTRAIT
                + " varchar, " + ParentInfo.TIMESTAMP + " biginteger, " + ParentInfo.RELATIONSHIP + " varchar, "
                + " UNIQUE(" + ParentInfo.PARENT_ID + ") " + ")");
    }

    @Override
    // 当数据库升级时，可以根据oldVersion，newVersion来做一些数据迁移，更新的操作
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        Log.d("Database", "onUpgrade begain");
        long current = System.currentTimeMillis();
        try{
            MyApplication.getInstance().setDbUpdating(true);
            new UpgradeDbHelper(db, this).upgradeDb();
        } catch(Exception e){
            e.printStackTrace();
        }
        finally{
            MyApplication.getInstance().setDbUpdating(false);
        }
        Log.d("Database", "onUpgrade over time =" + (System.currentTimeMillis() - current));
    }

    public void clearAll(SQLiteDatabase db){
        // db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TAB);
        // db.execSQL("DROP TABLE IF EXISTS " + BINDED_NUM_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + CHILDREN_INFO_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + SCHOOL_INFO_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + SCHEDULE_INFO_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + COOKBOOK_INFO_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + NEWS_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + SWIPE_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + HOMEWORK_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + EDUCATION_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + TEACHER_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + PARENT_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + NEW_CHAT_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + EXP_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + NATIVE_MEDIUM_URL_TAB);
        db.execSQL("DROP TABLE IF EXISTS " + RECEIPT_TAB);
        onCreate(db);
    }
}
