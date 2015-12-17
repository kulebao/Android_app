package com.cocobabys.dbmgr;

import java.util.ArrayList;
import java.util.List;

import com.cocobabys.dbmgr.info.IMGroupInfo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class IMGroupMgr{
    private SqliteHelper dbHelper;

    IMGroupMgr(SqliteHelper dbHelper){
        this.dbHelper = dbHelper;
    }

    long addInfo(IMGroupInfo info){
        ContentValues values = buildInfo(info);
        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        return writableDatabase.insertWithOnConflict(SqliteHelper.IM_GROUP_TAB, null, values,
                                                     SQLiteDatabase.CONFLICT_REPLACE);
    }

    void deleteGroup(String groupid){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + SqliteHelper.IM_GROUP_TAB + " WHERE " + IMGroupInfo.GROUP_ID + " ='" + groupid
                + "'");
    }

    private ContentValues buildInfo(IMGroupInfo info){
        ContentValues values = new ContentValues();
        values.put(IMGroupInfo.CLASS_ID, info.getClass_id());
        values.put(IMGroupInfo.GROUP_ID, info.getGroup_id());
        values.put(IMGroupInfo.GROUP_NAME, info.getGroup_name());
        return values;
    }

    List<IMGroupInfo> getAllIMGroupInfo(){
        List<IMGroupInfo> list = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.IM_GROUP_TAB, null);

        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast() && (cursor.getString(1) != null)){
                IMGroupInfo info = getInfoByCursor(cursor);
                list.add(info);
                cursor.moveToNext();
            }
        }
        finally{
            if(cursor != null){
                cursor.close();
            }
        }

        return list;
    }

    IMGroupInfo getInfo(int classid){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.IM_GROUP_TAB + " WHERE " + IMGroupInfo.CLASS_ID
                + " = " + classid, null);

        IMGroupInfo info = null;
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast() && (cursor.getString(1) != null)){
                info = getInfoByCursor(cursor);
                break;
            }
        }
        finally{
            if(cursor != null){
                cursor.close();
            }
        }

        return info;
    }

    IMGroupInfo getInfoByGroupID(String groupid){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.IM_GROUP_TAB + " WHERE " + IMGroupInfo.GROUP_ID
                + " = '" + groupid + "'", null);

        IMGroupInfo info = null;
        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast() && (cursor.getString(1) != null)){
                info = getInfoByCursor(cursor);
                break;
            }
        }
        finally{
            if(cursor != null){
                cursor.close();
            }
        }

        return info;
    }

    void clear(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + SqliteHelper.IM_GROUP_TAB);
    }

    private IMGroupInfo getInfoByCursor(Cursor cursor){
        IMGroupInfo info = new IMGroupInfo();

        info.setId(cursor.getInt(0));
        info.setClass_id(cursor.getInt(1));
        info.setGroup_id(cursor.getString(2));
        info.setGroup_name(cursor.getString(3));
        return info;
    }
}
