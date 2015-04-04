package com.cocobabys.net;

import org.apache.http.HttpStatus;

import android.util.Log;

import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ReceiptInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;

public class ReceiptMethod{
    private ReceiptMethod(){}

    public static ReceiptMethod getMethod(){
        return new ReceiptMethod();
    }

    public MethodResult getReceiptState(int newsID) throws Exception{
        HttpResult result = new HttpResult();
        String command = createCommand(newsID);
        Log.d("DDD getReceiptState ", " str : " + command);
        result = HttpClientHelper.executeGet(command);
        return getResult(result, newsID);
    }

    private MethodResult getResult(HttpResult result, int newsID) throws Exception{
        MethodResult methodResult = new MethodResult(EventType.GET_RECEIPT_SUCCESS);
        ReceiptInfo info = new ReceiptInfo();
        info.setReceipt_id(newsID);
        if(result.getResCode() == HttpStatus.SC_OK){
            Log.d("DDD getResult ", " setReceipt_state  1  newsID : " + newsID);
            info.setReceipt_state(1);
            DataMgr.getInstance().addReceiptInfo(info);
        } else if(result.getResCode() == HttpStatus.SC_NOT_FOUND){
            Log.d("DDD getResult ", " setReceipt_state  0  newsID : " + newsID);
            info.setReceipt_state(0);
            DataMgr.getInstance().addReceiptInfo(info);
        } else{
            methodResult.setResultType(EventType.GET_RECEIPT_FAIL);
        }
        return methodResult;
    }

    private String createCommand(int newsID){
        String cmd = String.format(ServerUrls.GET_RECEIPT_STATE, DataMgr.getInstance().getSchoolID(), newsID, DataMgr
                .getInstance().getSelfInfoByPhone().getParent_id());
        return cmd;
    }
}
