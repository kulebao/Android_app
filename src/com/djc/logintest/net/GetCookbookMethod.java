package com.djc.logintest.net;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.constant.ServerUrls;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.CookBookInfo;
import com.djc.logintest.dbmgr.info.InfoHelper;
import com.djc.logintest.httpclientmgr.HttpClientHelper;

public class GetCookbookMethod {

    private GetCookbookMethod() {
    }

    public static GetCookbookMethod getMethod() {
        return new GetCookbookMethod();
    }

    public int checkCookBook() {
        int bret = EventType.NET_WORK_INVALID;
        HttpResult result = new HttpResult();
        String url = createCookbookPreviewUrl();
        Log.e("DDDDD ", "createGetChildrenInfoUrl cmd:" + url);
        try {
            result = HttpClientHelper.executeGet(url);
            bret = handleCheckCookbookResult(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bret;
    }

    private String createCookbookPreviewUrl() {
        String url = String
                .format(ServerUrls.COOKBOOK_PRIVIEW, DataMgr.getInstance().getSchoolID());
        return url;
    }

    private int handleCheckCookbookResult(HttpResult result) {
        int event = EventType.NET_WORK_INVALID;
        if (result.getResCode() == HttpStatus.SC_OK) {
            try {
                // 返回单一元素数组，保存最新食谱
                JSONArray array = result.getJSONArray();
                JSONObject jsonObject = array.getJSONObject(0);
                Log.d("DDD handleGetChildInfoResult", "str : " + jsonObject.toString());
                int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);
                if (errorcode == 0) {
                    event = handleSuccess(jsonObject);
                } else {
                    event = EventType.GET_COOKBOOK_FAILED;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            event = EventType.SERVER_INNER_ERROR;
        }

        return event;
    }

    public int handleSuccess(JSONObject jsonObject) throws JSONException {
        String newtimestamp = jsonObject.getString(InfoHelper.TIMESTAMP);
        long newTime = Long.parseLong(newtimestamp);
        CookBookInfo cookBookInfo = DataMgr.getInstance().getCookBookInfo();

        if (cookBookInfo == null || (newTime > Long.parseLong(cookBookInfo.getTimestamp()))) {
            return getCookbook(jsonObject.getString(CookBookInfo.COOKBOOK_ID));
        }
        return EventType.GET_COOKBOOK_LATEST;
    }

    public int getCookbook(String cookbookID) {
        int bret = EventType.NET_WORK_INVALID;
        HttpResult result = new HttpResult();
        String url = createGetCookbookUrl(cookbookID);
        Log.e("DDDDD ", "getSchedule cmd:" + url);
        try {
            result = HttpClientHelper.executeGet(url);
            bret = handleGetCookbookResult(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bret;
    }

    private int handleGetCookbookResult(HttpResult result) {
        int event = EventType.NET_WORK_INVALID;
        if (result.getResCode() == HttpStatus.SC_OK) {
            try {
                JSONObject jsonObject = result.getJsonObject();
                Log.d("DDD handleGetChildInfoResult", "str : " + jsonObject.toString());
                int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);
                if (errorcode == 0) {
                    saveCookbook(jsonObject);
                    return EventType.GET_COOKBOOK_SUCCESS;
                } else {
                    event = EventType.GET_COOKBOOK_FAILED;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            event = EventType.SERVER_INNER_ERROR;
        }
        return event;
    }

    private String createGetCookbookUrl(String cookbookID) {
        String url = String.format(ServerUrls.COOKBOOK_DETAIL, DataMgr.getInstance().getSchoolID(),
                cookbookID);
        return url;
    }

    private void saveCookbook(JSONObject jsonObject) throws JSONException {
        CookBookInfo info = new CookBookInfo();
        info.setTimestamp(jsonObject.getString(InfoHelper.TIMESTAMP));
        info.setCookbook_id(jsonObject.getString(CookBookInfo.COOKBOOK_ID));
        info.setCookbook_content(jsonObject.getString(InfoHelper.WEEK_DETAIL));
        DataMgr.getInstance().updateCookBookInfo(info);
    }

}
