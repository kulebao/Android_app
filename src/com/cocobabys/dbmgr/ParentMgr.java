package com.cocobabys.dbmgr;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cocobabys.dbmgr.info.ParentInfo;

class ParentMgr{
    private SqliteHelper dbHelper;

    ParentMgr(SqliteHelper dbHelper){
        this.dbHelper = dbHelper;
    }

    void addData(ParentInfo info){
        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        ContentValues values = buildInfo(info);
        writableDatabase.insertWithOnConflict(SqliteHelper.PARENT_TAB, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    void addDataList(List<ParentInfo> list){
        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();

        writableDatabase.beginTransaction(); // 手动设置开始事务

        try{
            for(ParentInfo info : list){
                ContentValues values = buildInfo(info);
                writableDatabase.insertWithOnConflict(SqliteHelper.PARENT_TAB, null, values,
                                                      SQLiteDatabase.CONFLICT_REPLACE);
            }
            // 数据插入操作循环
            writableDatabase.setTransactionSuccessful(); // 设置事务处理成功，不设置会自动回滚不提交
        } catch(Exception e){
            e.printStackTrace();
        }
        writableDatabase.endTransaction(); // 处理完成
    }

    ParentInfo getParentByPhone(String phone){
        ParentInfo info = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.PARENT_TAB + " WHERE " + ParentInfo.PHONE + " = '"
                + phone + "'", null);

        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast() && (cursor.getString(1) != null)){
                info = getDataByCursor(cursor);
                cursor.moveToNext();
            }
        }
        finally{
            if(cursor != null){
                cursor.close();
            }
        }

        return info;
    }

    void updateAll(ParentInfo info, String parentid){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = buildInfo(info);
        db.update(SqliteHelper.PARENT_TAB, values, ParentInfo.PARENT_ID + "  = '" + parentid + "'", null);
    }

    void updateCardNum(String newCard, String parentid){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ParentInfo.CARD_NUMBER, newCard);
        db.update(SqliteHelper.PARENT_TAB, values, ParentInfo.PARENT_ID + "  = '" + parentid + "'", null);
    }

    ParentInfo getParentByInternalID(int internalid){
        ParentInfo info = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.PARENT_TAB + " WHERE " + ParentInfo.INTERNAL_ID
                + " = " + internalid, null);

        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast() && (cursor.getString(1) != null)){
                info = getDataByCursor(cursor);
                cursor.moveToNext();
            }
        }
        finally{
            if(cursor != null){
                cursor.close();
            }
        }

        return info;
    }

    ParentInfo getParentByID(String parentid){
        ParentInfo info = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.PARENT_TAB + " WHERE " + ParentInfo.PARENT_ID
                + " = '" + parentid + "'", null);

        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast() && (cursor.getString(1) != null)){
                info = getDataByCursor(cursor);
                cursor.moveToNext();
            }
        }
        finally{
            if(cursor != null){
                cursor.close();
            }
        }

        return info;
    }

    ContentValues buildInfo(ParentInfo info){
        ContentValues values = new ContentValues();
        values.put(ParentInfo.CARD_NUMBER, info.getCard());
        values.put(ParentInfo.MEMBER_STATUS, info.getMember_status());
        values.put(ParentInfo.PARENT_ID, info.getParent_id());
        values.put(ParentInfo.PARENT_NAME, info.getName());
        values.put(ParentInfo.PHONE, info.getPhone());
        values.put(ParentInfo.PORTRAIT, info.getPortrait());
        values.put(ParentInfo.TIMESTAMP, info.getTimestamp());
        values.put(ParentInfo.RELATIONSHIP, info.getRelationship());
        values.put(ParentInfo.INTERNAL_ID, info.getInternal_id());
        return values;
    }

    private ParentInfo getDataByCursor(Cursor cursor){
        ParentInfo info = new ParentInfo();

        info.setId(cursor.getInt(0));
        info.setCard(cursor.getString(1));
        info.setMember_status(cursor.getInt(2));
        info.setParent_id(cursor.getString(3));
        info.setName(cursor.getString(4));
        info.setPhone(cursor.getString(5));
        info.setPortrait(cursor.getString(6));
        info.setTimestamp(cursor.getLong(7));
        info.setRelationship(cursor.getString(8));
        info.setInternal_id(cursor.getInt(9));
        return info;
    }
}
