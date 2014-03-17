package com.djc.logintest.net;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.constant.ServerUrls;
import com.djc.logintest.httpclientmgr.HttpClientHelper;

public class GetAuthCodeMethod {
    private GetAuthCodeMethod() {
    }

    public static GetAuthCodeMethod getGetAuthCodeMethod() {
        return new GetAuthCodeMethod();
    }

    public int getAuthCode(String phone, int type) {
        int bret = EventType.NET_WORK_INVALID;
        HttpResult result = new HttpResult();
        String command = createGetAuthCodeCommand(phone);
        try {
            String url = getUrl(type);
            result = HttpClientHelper.executePost(url, command);
            bret = handleGetAuthCodeResult(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bret;
    }

    private String getUrl(int type) {
        return (type == ConstantValue.TYPE_GET_REG_AUTHCODE) ? ServerUrls.GET_REG_AUTHCODE
                : ServerUrls.GET_RESET_PWD_AUTHCODE;
    }

    private String createGetAuthCodeCommand(String phone) {
        JSONObject object = new JSONObject();
        try {
            object.put(JSONConstant.PHONE_NUM, phone);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    private int handleGetAuthCodeResult(HttpResult result) {
        int event = EventType.NET_WORK_INVALID;
        if (result.getResCode() == HttpStatus.SC_OK) {
            try {
                JSONObject jsonObject = result.getJsonObject();
                Log.d("DDD changePwd", "str : " + jsonObject.toString());

                int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);

                // 登录成功，保存token
                if (errorcode == 0) {
                    event = EventType.GET_AUTH_CODE_SUCCESS;
                } else {
                    event = EventType.GET_AUTH_CODE_FAIL;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return event;
    }
}
