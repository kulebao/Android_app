package com.cocobabys.dbmgr;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cocobabys.dbmgr.info.InfoHelper;
import com.cocobabys.dbmgr.info.SchoolInfo;

public class SchoolInfoMgr {
    private SqliteHelper dbHelper;

    public SchoolInfoMgr(SqliteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    long addSchoolInfo(SchoolInfo info) {
        ContentValues values = setProp(info);
        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        return writableDatabase.insert(SqliteHelper.SCHOOL_INFO_TAB, null, values);
    }

    private ContentValues setProp(SchoolInfo info) {
        ContentValues values = new ContentValues();
        values.put(SchoolInfo.SCHOOL_DESC, info.getSchool_desc());
        values.put(SchoolInfo.SCHOOL_ID, info.getSchool_id());
        values.put(SchoolInfo.SCHOOL_LOCAL_URL, info.getSchool_logo_local_url());
        values.put(SchoolInfo.SCHOOL_NAME, info.getSchool_name());
        values.put(SchoolInfo.SCHOOL_PHONE, info.getSchool_phone());
        values.put(InfoHelper.TIMESTAMP, info.getTimestamp());
        values.put(SchoolInfo.SCHOOL_SERVER_URL, info.getSchool_logo_server_url());
        return values;
    }

    void updateSchoolLogoLocalUrl(String schoolid, String localurl) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SchoolInfo.SCHOOL_LOCAL_URL, localurl);
        db.update(SqliteHelper.SCHOOL_INFO_TAB, values, SchoolInfo.SCHOOL_ID + "  = " + schoolid,
                null);
    }

    void updateSchoolInfo(String schoolid, SchoolInfo info) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = setProp(info);
        db.update(SqliteHelper.SCHOOL_INFO_TAB, values, SchoolInfo.SCHOOL_ID + "  = " + schoolid,
                null);
    }

    SchoolInfo getSchoolInfo() {
        SchoolInfo info = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.SCHOOL_INFO_TAB, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
                info = getSchoolInfoByCursor(cursor);
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

    private SchoolInfo getSchoolInfoByCursor(Cursor cursor) {
        SchoolInfo info = new SchoolInfo();

        info.setId(cursor.getInt(0));
        info.setSchool_id(cursor.getString(1));
        info.setSchool_name(cursor.getString(2));
        info.setSchool_phone(cursor.getString(3));
        info.setSchool_desc(cursor.getString(4));
        info.setSchool_logo_local_url(cursor.getString(5));
        info.setSchool_logo_server_url(cursor.getString(6));
        info.setTimestamp(cursor.getString(7));
        return info;
    }
}
