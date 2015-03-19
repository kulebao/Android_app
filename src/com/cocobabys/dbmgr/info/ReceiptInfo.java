package com.cocobabys.dbmgr.info;

public class ReceiptInfo{
    public static final String ID            = "_id";
    public static final String RECEIPT_ID    = "receipt_id";
    public static final String RECEIPT_STATE = "receipt_state";

    private long               id            = 0;
    private long               receipt_id    = 0;

    // 0表示未回执，1表示已回执
    private int                receipt_state = 0;

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public long getReceipt_id(){
        return receipt_id;
    }

    public void setReceipt_id(long receipt_id){
        this.receipt_id = receipt_id;
    }

    public int getReceipt_state(){
        return receipt_state;
    }

    public void setReceipt_state(int receipt_state){
        this.receipt_state = receipt_state;
    }

}
