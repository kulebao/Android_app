package com.djc.logintest.push.info;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.utils.Utils;

public class JsonHelper {
    
    public static void saveBindInfo(String content) {
        try {
            JSONObject jsonContent = new JSONObject(content);
            JSONObject params = jsonContent.getJSONObject("response_params");
//            String appid = params.getString("appid");
            String channelid = params.getString(JSONConstant.CHANNEL_ID);
            String userid = params.getString(JSONConstant.USER_ID);
            Log.d("bbind","saveBindInfo userid="+userid);
            Utils.saveUndeleteableProp(JSONConstant.CHANNEL_ID, channelid);
            Utils.saveUndeleteableProp(JSONConstant.USER_ID, userid);
        } catch (JSONException e) {
            Log.e("", "Parse bind json infos error: " + e);
        }
    }
}
