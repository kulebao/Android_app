package com.cocobabys.net;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONException;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.cocobabys.bean.GroupExpInfo;
import com.cocobabys.bean.ShareToken;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ExpInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;

public class ExpMethod{

    private ExpMethod(){}

    public static ExpMethod getMethod(){
        return new ExpMethod();
    }

    public MethodResult getExpCount(int year) throws Exception{
        MethodResult methodResult = new MethodResult(EventType.GET_EXP_COUNT_SUCCESS);
        HttpResult result = new HttpResult();
        String url = createGetExpCountUrl(year);
        Log.d("DJC", "getExpCount cmd:" + url);
        result = HttpClientHelper.executeGet(url);
        if(result.getResCode() != HttpStatus.SC_OK){
            methodResult.setResultType(EventType.GET_EXP_COUNT_FAIL);
            return methodResult;
        }
        List<GroupExpInfo> list = handleGetExpCountResult(result);
        methodResult.setResultObj(list);
        return methodResult;
    }

    private String createGetExpCountUrl(int year){
        String url = String.format(ServerUrls.GET_EXP_COUNT, DataMgr.getInstance().getSchoolID(), DataMgr.getInstance()
                .getSelectedChild().getServer_id());
        url += "year=" + year;
        return url;
    }

    private List<GroupExpInfo> handleGetExpCountResult(HttpResult result) throws JSONException{
        return GroupExpInfo.jsonArrayToGroupExpInfoList(result.getJSONArray());
    }

    public MethodResult getExpInfoByYearAndMonth(int year, String month) throws Exception{
        MethodResult methodResult = new MethodResult(EventType.GET_EXP_INFO_SUCCESS);
        HttpResult result = new HttpResult();
        String url = createGetExpInfoUrl(year, month);
        Log.d("DJC", "getExpInfoByYearAndMonth cmd:" + url);
        result = HttpClientHelper.executeGet(url);
        if(result.getResCode() != HttpStatus.SC_OK){
            methodResult.setResultType(EventType.GET_EXP_INFO_FAIL);
            return methodResult;
        }

        handleGetExpInfoResult(result);
        return methodResult;
    }

    private void handleGetExpInfoResult(HttpResult result) throws JSONException{
        List<ExpInfo> list = ExpInfo.parseFromJsonArray(result.getJSONArray());
        DataMgr.getInstance().addExpDataList(list);
    }

    private String createGetExpInfoUrl(int year, String month){
        String url = String.format(ServerUrls.GET_EXP_INFO, DataMgr.getInstance().getSchoolID(), DataMgr.getInstance()
                .getSelectedChild().getServer_id());

        url += "month=" + year + month;
        return url;
    }

    public MethodResult sendExp(String content) throws Exception{
        MethodResult methodResult = new MethodResult(EventType.POST_EXP_SUCCESS);
        HttpResult result = new HttpResult();
        String url = createSendExpUrl();
        Log.e("DJC", "uploadChildInfo cmd:" + url + " content=" + content);
        result = HttpClientHelper.executePost(url, content);
        if(result.getResCode() != HttpStatus.SC_OK){
            methodResult.setResultType(EventType.POST_EXP_FAIL);
            return methodResult;
        }

        ExpInfo info = saveExpInfoToDB(result);
        methodResult.setResultObj(info);
        return methodResult;
    }

    public MethodResult getShareToken(ExpInfo info) throws Exception{
        MethodResult methodResult = new MethodResult(EventType.GET_EXP_TOKEN_SUCCESS);
        HttpResult result = new HttpResult();
        String url = info.buildShareUrl();
        Log.d("DJC", "uploadChildInfo cmd:" + url);
        result = HttpClientHelper.executePost(url, "");
        Log.d("DJC", "uploadChildInfo result:" + result.toString());

        if(result.getResCode() != HttpStatus.SC_OK){
            methodResult.setResultType(EventType.GET_EXP_TOKEN_FAIL);
            return methodResult;
        }

        // ShareToken shareToken = new ShareToken();
        // String token = result.getJsonObject().getString("token");
        // shareToken.setToken(token);

        ShareToken shareToken = JSON.parseObject(result.getContent(), ShareToken.class);
        // 这里要纠错一下，服务器返回的并不是exp的id
        shareToken.setId(info.getExp_id());

        Log.d("DJC", "uploadChildInfo shareToken:" + shareToken.toString());

        methodResult.setResultObj(shareToken);
        return methodResult;
    }

    private ExpInfo saveExpInfoToDB(HttpResult result) throws JSONException{
        ExpInfo info = ExpInfo.parseFromJsonObj(result.getJsonObject());
        List<ExpInfo> list = new ArrayList<ExpInfo>();
        list.add(info);
        DataMgr.getInstance().addExpDataList(list);
        return info;
    }

    private String createSendExpUrl(){
        String url = String.format(ServerUrls.GET_EXP_INFO, DataMgr.getInstance().getSchoolID(), DataMgr.getInstance()
                .getSelectedChild().getServer_id());
        return url;
    }

    public MethodResult deleteExp(long expid, String childid) throws Exception{
        MethodResult methodResult = new MethodResult(EventType.DELETE_EXP_SUCCESS);
        HttpResult result = new HttpResult();
        String url = createDeleteExpUrl(expid, childid);
        Log.e("DDDDD ", "deleteChat cmd:" + url + " content=" + expid);
        result = HttpClientHelper.executeDelete(url);
        Log.e("DDDDD ", " result =" + result.getContent());
        if(result.getResCode() != HttpStatus.SC_OK){
            methodResult.setResultType(EventType.DELETE_EXP_FAIL);
        }
        return methodResult;
    }

    private String createDeleteExpUrl(long expid, String childid){
        String url = String.format(ServerUrls.DELETE_EXP, DataMgr.getInstance().getSchoolID(), childid, expid);
        return url;
    }
}
