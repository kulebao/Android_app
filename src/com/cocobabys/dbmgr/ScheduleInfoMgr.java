package com.cocobabys.dbmgr;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cocobabys.dbmgr.info.InfoHelper;
import com.cocobabys.dbmgr.info.ScheduleInfo;

public class ScheduleInfoMgr {
    private SqliteHelper dbHelper;

    public ScheduleInfoMgr(SqliteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    long addScheduleInfo(ScheduleInfo info) {
        ContentValues values = new ContentValues();
        values.put(ScheduleInfo.SCHEDULE_ID, info.getSchedule_id());
        values.put(ScheduleInfo.SCHEDULE_CONTENT, info.getSchedule_content());
        values.put(InfoHelper.TIMESTAMP, info.getTimestamp());
        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        return writableDatabase.insert(SqliteHelper.SCHEDULE_INFO_TAB, null, values);
    }

    // 只保留最新一条ScheduleInfo记录，更新就是先删除旧的再插入新的
    void updateScheduleInfo(ScheduleInfo info) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + SqliteHelper.SCHEDULE_INFO_TAB);
        addScheduleInfo(info);
    }

    ScheduleInfo getScheduleInfo() {
        ScheduleInfo info = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.SCHEDULE_INFO_TAB, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
                info = getScheduleInfoByCursor(cursor);
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

    private ScheduleInfo getScheduleInfoByCursor(Cursor cursor) {
        ScheduleInfo info = new ScheduleInfo();

        info.setId(cursor.getInt(0));
        info.setSchedule_id(cursor.getString(1));
        info.setSchedule_content(cursor.getString(2));
        info.setTimestamp(cursor.getString(3));
        return info;
    }
}
