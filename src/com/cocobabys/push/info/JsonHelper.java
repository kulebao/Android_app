package com.cocobabys.push.info;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.cocobabys.constant.JSONConstant;
import com.cocobabys.utils.DataUtils;

public class JsonHelper {
    
    public static void saveBindInfo(String content) {
        try {
            JSONObject jsonContent = new JSONObject(content);
            JSONObject params = jsonContent.getJSONObject("response_params");
//            String appid = params.getString("appid");
            String channelid = params.getString(JSONConstant.CHANNEL_ID);
            String userid = params.getString(JSONConstant.USER_ID);
            Log.d("bbind","saveBindInfo userid="+userid);
            DataUtils.saveUndeleteableProp(JSONConstant.CHANNEL_ID, channelid);
            DataUtils.saveUndeleteableProp(JSONConstant.USER_ID, userid);
        } catch (JSONException e) {
            Log.e("", "Parse bind json infos error: " + e);
        }
    }
}
