package com.cocobabys.push;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.android.pushservice.PushSettings;
import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ChildInfo;
import com.cocobabys.utils.DataUtils;

import android.text.TextUtils;
import android.util.Log;

public class PushModel{
    // 测试服使用的push apikey
    private static String DEBUG_API_KEY   = "9mzy6mOGMormOggT67K3jqBg";

    // 正式服服使用的push apikey
    private static String RELEASE_API_KEY = "O7Xwbt4DWOzsji57xybprqUc";

    private String getApiKey(){
        return MyApplication.getInstance().isForTest() ? DEBUG_API_KEY : RELEASE_API_KEY;
    }

    private PushModel(){

    }

    public void enableDebug(boolean enable){
        PushSettings.enableDebugMode(MyApplication.getInstance(), enable);
    }

    public static PushModel getPushModel(){
        return new PushModel();
    }

    public void bind(){
        Log.d("bbind", "do bind!");
        PushManager.startWork(MyApplication.getInstance(), PushConstants.LOGIN_TYPE_API_KEY, getApiKey());
    }

    public void unBind(){
        PushManager.stopWork(MyApplication.getInstance());
    }

    public void setTag(List<String> tags){
        PushManager.setTags(MyApplication.getInstance(), tags);
    }

    // 设置学校id和班级id为默认tag,注意调用时机为，学校信息和小孩信息都同时获取到之后
    public void setSchoolTag(){
        List<String> allTags = getTags();
        String schoolID = DataMgr.getInstance().getSchoolID();

        if(!allTags.isEmpty() && allTags.contains(schoolID)){
            Log.d("", "already set schooltag =" + schoolID);
            return;
        }

        try{
            List<String> tags = new ArrayList<String>();

            if(!TextUtils.isEmpty(schoolID)){
                tags.add(schoolID);
                Log.d("DJC 10-16", "setTag tags=" + tags);
                PushModel.getPushModel().setTag(tags);
            }

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    // 设置学校id和班级id为默认tag,注意调用时机为，学校信息和小孩信息都同时获取到之后
    public void setAllDefaultTag(){
        try{
            List<String> tags = new ArrayList<String>();
            String schoolTag = DataMgr.getInstance().getSchoolID();
            List<ChildInfo> allChildrenInfo = DataMgr.getInstance().getAllChildrenInfo();

            if(!allChildrenInfo.isEmpty() && !"".equals(schoolTag)){
                for(ChildInfo info : allChildrenInfo){
                    if(!TextUtils.isEmpty(info.getClass_id())){
                        tags.add(info.getClass_id());
                    }
                }
                tags.add(schoolTag);
                Log.d("DJC 10-16", "setTag tags=" + tags);
                PushModel.getPushModel().setTag(tags);
            }

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean isBinded(){
        return PushManager.isPushEnabled(MyApplication.getInstance());
    }

    public boolean isBindInfoSentToServer(){
        String user_id = DataUtils.getUndeleteableProp(JSONConstant.USER_ID);
        String channel_id = DataUtils.getUndeleteableProp(JSONConstant.CHANNEL_ID);
        Log.d("", "BindPushTask pusid =" + user_id + "\nchannel_id=" + channel_id);
        return !ConstantValue.FAKE_USER_ID.equals(user_id);
    }

    public List<String> getTags(){
        List<String> tags = new ArrayList<String>();
        String tagsStr = DataUtils.getUndeleteableProp(JSONConstant.PUSH_TAGS);
        Log.d("DJC 10-16", "tags =" + tagsStr);
        if(!"".equals(tagsStr)){
            String[] split = tagsStr.split(",");
            tags = Arrays.asList(split);
        }

        return tags;
    }
}
