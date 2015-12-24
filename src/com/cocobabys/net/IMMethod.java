package com.cocobabys.net;

import org.apache.http.HttpStatus;

import com.alibaba.fastjson.JSONObject;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.IMGroupInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;

import android.util.Log;

public class IMMethod {
	private IMMethod() {
	}

	public static IMMethod getMethod() {
		return new IMMethod();
	}

	public MethodResult getGroupInfo(String classid) throws Exception {
		HttpResult result = new HttpResult();
		String command = createGetGroupCommand(classid);
		Log.d("", " DDD getGroupInfo getInfo  command: " + command);
		result = HttpClientHelper.executeGet(command);
		Log.d("", " DDD getGroupInfo getInfo result : " + result.getContent());
		return getGroupResult(result);
	}

	public MethodResult joinGroupInfo(String classid) throws Exception {
		HttpResult result = new HttpResult();
		String command = createJoinGroupCommand(classid);
		Log.d("", " DDD joinGroupInfo getInfo  command: " + command);
		result = HttpClientHelper.executePost(command, "{}");
		Log.d("", " DDD joinGroupInfo getInfo result : " + result.getContent());
		return getJoinGroupResult(result);
	}
	
	public boolean quitGroupInfo(String classid){
	    HttpResult result = new HttpResult();;
        try{
            String command = createQuitGroupCommand(classid);
            Log.d("", " DDD quitGroupInfo getInfo  command: " + command);
            result = HttpClientHelper.executeDelete(command);
        } catch(Exception e){
            e.printStackTrace();
        }
        Log.d("", " DDD quitGroupInfo getInfo result : " + result.getContent());
	    return result.getResCode() == HttpStatus.SC_OK;
	}

	private String createQuitGroupCommand(String classid){
        String cmd = String.format(ServerUrls.JOIN_GROUP_INFO_URL, DataMgr.getInstance().getSchoolID(), classid);
        cmd = cmd + "/me";
        return cmd;
    }

    private MethodResult getJoinGroupResult(HttpResult result) {
		MethodResult methodResult = new MethodResult(EventType.JOIN_IM_GROUP_FAIL);
		if (result.getResCode() == HttpStatus.SC_OK) {
			IMGroupInfo groupinfo = JSONObject.parseObject(result.getContent(), IMGroupInfo.class);
			DataMgr.getInstance().addIMGroupInfo(groupinfo);
			methodResult.setResultObj(groupinfo);
			methodResult.setResultType(EventType.JOIN_IM_GROUP_SUCCESS);
		}
		return methodResult;
	}

	private String createJoinGroupCommand(String classid) {
		String cmd = String.format(ServerUrls.JOIN_GROUP_INFO_URL, DataMgr.getInstance().getSchoolID(), classid);
		return cmd;
	}

	private MethodResult getGroupResult(HttpResult result) throws Exception {
		MethodResult methodResult = new MethodResult(EventType.GET_IM_GROUP_FAIL);
		if (result.getResCode() == HttpStatus.SC_OK) {
			IMGroupInfo groupinfo = JSONObject.parseObject(result.getContent(), IMGroupInfo.class);
			methodResult.setResultObj(groupinfo);
			methodResult.setResultType(EventType.GET_IM_GROUP_SUCCESS);
		}
		return methodResult;
	}

	private String createGetGroupCommand(String classid) {
		String cmd = String.format(ServerUrls.GET_GROUP_INFO_URL, DataMgr.getInstance().getSchoolID(), classid);
		return cmd;
	}
}
