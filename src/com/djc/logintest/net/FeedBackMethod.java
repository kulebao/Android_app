package com.djc.logintest.net;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.constant.ServerUrls;
import com.djc.logintest.httpclientmgr.HttpClientHelper;
import com.djc.logintest.utils.Utils;

public class FeedBackMethod {

    private static final String PHONE_NUM = "phone";
    private static final String FEEDBACK_CONTENT = "content";

    private FeedBackMethod() {
    }

    public static FeedBackMethod getMethod() {
        return new FeedBackMethod();
    }

    public int feedBack(String content) {
        int bret = EventType.NET_WORK_INVALID;
        HttpResult result = new HttpResult();
        String url = ServerUrls.FEED_BACK;
        Log.e("DDDDD ", "uploadChildInfo cmd:" + url);
        String command = formatContent(content);
        try {
            result = HttpClientHelper.executePost(url, command);
            bret = handleFeedbackResult(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bret;
    }

    private String formatContent(String content) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PHONE_NUM, Utils.getProp(JSONConstant.ACCOUNT_NAME));
            jsonObject.put(FEEDBACK_CONTENT, content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

    private int handleFeedbackResult(HttpResult result) {
        int event = EventType.SERVER_BUSY;
        if (result.getResCode() == HttpStatus.SC_OK) {
            try {
                JSONObject jsonObject = result.getJsonObject();
                Log.d("DDD handleGetChildInfoResult", "str : " + jsonObject.toString());
                int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);
                // 登录成功，保存token
                if (errorcode == 0) {
                    event = EventType.UPLOAD_SUCCESS;
                } else {
                    event = EventType.UPLOAD_FAILED;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            event = EventType.SERVER_INNER_ERROR;
        }

        return event;
    }

}
