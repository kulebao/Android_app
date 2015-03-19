package com.cocobabys.dbmgr;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cocobabys.dbmgr.info.ReceiptInfo;

class ReceiptMgr{
    private SqliteHelper dbHelper;

    ReceiptMgr(SqliteHelper dbHelper){
        this.dbHelper = dbHelper;
    }

    long addInfo(ReceiptInfo info){
        ContentValues values = buildInfo(info);
        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        return writableDatabase.insertWithOnConflict(SqliteHelper.RECEIPT_TAB, null, values,
                                                     SQLiteDatabase.CONFLICT_REPLACE);
    }

    private ContentValues buildInfo(ReceiptInfo info){
        ContentValues values = new ContentValues();
        values.put(ReceiptInfo.RECEIPT_ID, info.getReceipt_id());
        values.put(ReceiptInfo.RECEIPT_STATE, info.getReceipt_state());
        return values;
    }

    ReceiptInfo getInfo(int receiptID){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + SqliteHelper.RECEIPT_TAB + " WHERE " + ReceiptInfo.RECEIPT_ID
                + " = " + receiptID, null);

        ReceiptInfo info = null;
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
        db.execSQL("DELETE FROM " + SqliteHelper.RECEIPT_TAB);
    }

    private ReceiptInfo getInfoByCursor(Cursor cursor){
        ReceiptInfo info = new ReceiptInfo();

        info.setId(cursor.getInt(0));
        info.setReceipt_id(cursor.getLong(1));
        info.setReceipt_state(cursor.getInt(2));
        return info;
    }
}
