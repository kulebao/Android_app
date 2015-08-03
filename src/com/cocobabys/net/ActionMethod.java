package com.cocobabys.net;

import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.cocobabys.bean.ActionInfo;
import com.cocobabys.bean.PullToRefreshListInfo;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.utils.MethodUtils;

public class ActionMethod{
    private ActionMethod(){}

    public static ActionMethod getMethod(){
        return new ActionMethod();
    }

    public MethodResult getInfo(PullToRefreshListInfo info) throws Exception{
        HttpResult result = new HttpResult();
        String command = createGetInfoCommand(info);
        Log.d("DDD ActionMethod getInfo", " str : " + command);
        result = HttpClientHelper.executeGet(command);
        return getInfoResult(result);
    }

    public MethodResult getInfoBelongToMerchant(int merchantID) throws Exception{
        HttpResult result = new HttpResult();
        String command = createGetInfoBelongToMerchantCommand(merchantID);
        Log.d("DDD ActionMethod getInfo", " str : " + command);
        result = HttpClientHelper.executeGet(command);
        return getInfoResult(result);
    }

    private MethodResult getInfoResult(HttpResult result) throws Exception{
        Log.d("DDD ActionMethod getResult", " result : " + result.toString());
        MethodResult methodResult = new MethodResult(EventType.ACTION_GET_FAIL);
        if(result.getResCode() == HttpStatus.SC_OK){
            List<ActionInfo> list = JSON.parseArray(result.getContent(), ActionInfo.class);

            methodResult.setResultObj(list);
            methodResult.setResultType(EventType.ACTION_GET_SUCCESS);
        }
        return methodResult;
    }

    private String createGetInfoBelongToMerchantCommand(int id){
        String cmd = String.format(ServerUrls.GET_MERCHANT_ACTION_LIST, DataMgr.getInstance().getSchoolID(), id);

        return cmd;
    }

    private String createGetInfoCommand(PullToRefreshListInfo info){
        String cmd = String.format(ServerUrls.GET_ACTION_LIST, DataMgr.getInstance().getSchoolID());

        String createFromToParams = MethodUtils.createFromToParams(info);
        return cmd + createFromToParams;
    }

    public MethodResult getEnroll(int actionID) throws Exception{
        HttpResult result = new HttpResult();
        String command = createGetEnrollStateCommand(actionID);
        Log.d("DDD ActionMethod getInfo", " str : " + command);
        result = HttpClientHelper.executeGet(command);
        return getCheckEnrollResult(result);
    }

    private MethodResult getCheckEnrollResult(HttpResult result){
        Log.d("DDD ActionMethod getEnrollResult", " result : " + result.toString());
        MethodResult methodResult = new MethodResult(EventType.ACTION_GET_ENROLL_FAIL);
        // 成功就表示报过名了，否则返回404
        if(result.getResCode() == HttpStatus.SC_OK){
            methodResult.setResultType(EventType.ACTION_ENROLLED);
        } else if(result.getResCode() == HttpStatus.SC_NOT_FOUND){
            methodResult.setResultType(EventType.ACTION_NOT_ENROLL);
        }
        return methodResult;
    }

    private String createGetEnrollStateCommand(int actionID){
        String cmd = String.format(ServerUrls.GET_ENROLL_STATE, DataMgr.getInstance().getSchoolID(), actionID, DataMgr
                .getInstance().getSelfInfoByPhone().getParent_id());
        return cmd;
    }

    public MethodResult doEnroll(ActionInfo actionInfo) throws Exception{
        HttpResult result = new HttpResult();
        String url = createDoEnrollCommand(actionInfo.getId());
        Log.d("DDD ActionMethod getInfo", " str : " + url);
        String content = getDoEnrollContent(actionInfo);
        result = HttpClientHelper.executePost(url, content);
        return getDoEnrollResult(result);
    }

    private MethodResult getDoEnrollResult(HttpResult result){
        Log.d("DDD ActionMethod getDoEnrollResult", " result : " + result.toString());
        MethodResult methodResult = new MethodResult(EventType.ACTION_DO_ENROLL_FAIL);
        if(result.getResCode() == HttpStatus.SC_OK){
            methodResult.setResultType(EventType.ACTION_DO_ENROLL_SUCCESS);
        }
        return methodResult;
    }

    private String getDoEnrollContent(ActionInfo actionInfo) throws JSONException{
        JSONObject object = new JSONObject();
        DataMgr instance = DataMgr.getInstance();
        object.put("agent_id", actionInfo.getAgent_id());
        object.put("activity_id", actionInfo.getId());
        object.put("school_id", Integer.parseInt(instance.getSchoolID()));
        ParentInfo parentinfo = instance.getSelfInfoByPhone();
        object.put("parent_id", parentinfo.getParent_id());
        object.put("contact", parentinfo.getPhone());
        object.put("name", parentinfo.getName());

        return object.toString();
    }

    private String createDoEnrollCommand(int actionID){
        String cmd = String.format(ServerUrls.DO_ENROLL, DataMgr.getInstance().getSchoolID(), actionID);
        return cmd;
    }
}
