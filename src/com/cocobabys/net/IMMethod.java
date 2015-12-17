package com.cocobabys.net;

import org.apache.http.HttpStatus;

import com.alibaba.fastjson.JSONObject;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.IMGroupInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;

import android.util.Log;

public class IMMethod{
    private IMMethod(){}

    public static IMMethod getMethod(){
        return new IMMethod();
    }

    public MethodResult getGroupInfo(String classid) throws Exception{
        HttpResult result = new HttpResult();
        String command = createCommand(classid);
        Log.d("", " DDD getGroupInfo getInfo  command: " + command);
        result = HttpClientHelper.executeGet(command);
        Log.d("", " DDD getGroupInfo getInfo result : " + result.getContent());
        return getResult(result);
    }

    private MethodResult getResult(HttpResult result) throws Exception{
        MethodResult methodResult = new MethodResult(EventType.GET_IM_GROUP_FAIL);
        if(result.getResCode() == HttpStatus.SC_OK){
            IMGroupInfo groupinfo = JSONObject.parseObject(result.getContent(), IMGroupInfo.class);
            methodResult.setResultObj(groupinfo);
            methodResult.setResultType(EventType.GET_IM_GROUP_SUCCESS);
        }
        return methodResult;
    }

    private String createCommand(String classid){
        String cmd = String.format(ServerUrls.GET_GROUP_INFO_URL, DataMgr.getInstance().getSchoolID(), classid);
        return cmd;
    }
}
