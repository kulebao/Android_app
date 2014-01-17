package com.djc.logintest.dbmgr;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.djc.logintest.dbmgr.info.CookBookInfo;
import com.djc.logintest.dbmgr.info.InfoHelper;

public class CookBookInfoMgr {
    private SqliteHelper dbHelper;

    public CookBookInfoMgr(SqliteHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    long addCookBookInfo(CookBookInfo info) {
        ContentValues values = new ContentValues();
        values.put(CookBookInfo.COOKBOOK_ID, info.getCookbook_id());
        values.put(CookBookInfo.COOKBOOK_CONTENT, info.getCookbook_content());
        values.put(InfoHelper.TIMESTAMP, info.getTimestamp());
        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        return writableDatabase.insert(SqliteHelper.COOKBOOK_INFO_TAB, null, values);
    }

    // 只保留最新一条CookBookInfo记录，更新就是先删除旧的再插入新的
    void updateCookBookInfo(CookBookInfo info) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + SqliteHelper.COOKBOOK_INFO_TAB);
        addCookBookInfo(info);
    }

    CookBookInfo getCookBookInfo() {
        CookBookInfo info = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.COOKBOOK_INFO_TAB, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
                info = getCookBookInfoByCursor(cursor);
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

    private CookBookInfo getCookBookInfoByCursor(Cursor cursor) {
        CookBookInfo info = new CookBookInfo();

        info.setId(cursor.getInt(0));
        info.setCookbook_id(cursor.getString(1));
        info.setCookbook_content(cursor.getString(2));
        info.setTimestamp(cursor.getString(3));
        return info;
    }
}
