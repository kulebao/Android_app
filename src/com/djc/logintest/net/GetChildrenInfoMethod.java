package com.djc.logintest.net;

import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.constant.ServerUrls;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.ChildInfo;
import com.djc.logintest.httpclientmgr.HttpClientHelper;
import com.djc.logintest.utils.Utils;

public class GetChildrenInfoMethod {

    private GetChildrenInfoMethod() {
    }

    public static GetChildrenInfoMethod getMethod() {
        return new GetChildrenInfoMethod();
    }

    public int updateChildrenInfo() {
        int bret = EventType.NET_WORK_INVALID;
        HttpResult result = new HttpResult();
        String url = createAllGetChildrenInfoUrl();
        Log.e("DDDDD ", "createGetChildrenInfoUrl cmd:" + url);
        try {
            result = HttpClientHelper.executeGet(url);
            bret = handleGetChildInfoResult(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bret;
    }

    private String createAllGetChildrenInfoUrl() {
        String url = String.format(ServerUrls.GET_ALL_CHILDREN_INFO, DataMgr.getInstance()
                .getSchoolID(), Utils.getProp(JSONConstant.ACCOUNT_NAME));
        return url;
    }

    private int handleGetChildInfoResult(HttpResult result) {
        int event = EventType.NET_WORK_INVALID;
        if (result.getResCode() == HttpStatus.SC_OK) {
            try {
                JSONObject jsonObject = result.getJsonObject();
                Log.d("DDD handleGetChildInfoResult", "str : " + jsonObject.toString());
                int errorcode = jsonObject.getInt(JSONConstant.ERROR_CODE);
                // 登录成功，保存token
                if (errorcode == 0) {
                    event = checkUpdate(jsonObject);
                } else {
                    event = EventType.GET_CHILDREN_INFO_FAILED;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            event = EventType.SERVER_INNER_ERROR;
        }

        return event;
    }

    public int checkUpdate(JSONObject jsonObject) throws JSONException {
        int event = EventType.CHILDREN_INFO_IS_LATEST;
        String jsonSrc = jsonObject.getString("children");
        JSONArray array = new JSONArray(jsonSrc);
        ChildInfo selectedChild = DataMgr.getInstance().getSelectedChild();
        if (selectedChild == null) {
            // 首次使用，把全部小孩数据更新到数据库
            updateAll(array);
            event = EventType.UPDATE_CHILDREN_INFO;
        } else {
            // 只对选中小孩进行判断是否更新
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                ChildInfo childinfo = ChildInfo.jsonObjToChildInfo(obj);
                if (childinfo.getServer_id().equals(selectedChild.getServer_id())) {
                    // 检查更新时间
                    if (Long.parseLong(childinfo.getTimestamp()) > Long.parseLong(selectedChild
                            .getTimestamp())) {
                        DataMgr.getInstance().updateChildInfo(childinfo.getServer_id(), childinfo);
                        event = EventType.UPDATE_CHILDREN_INFO;
                        break;
                    }
                }
            }
        }
        //这里绑定tag，因为学校id在登录成功后必定保存，班级id在此有可能获取成功
        Utils.bindPushTags();
        return event;
    }

    private void updateAll(JSONArray array) {
        List<ChildInfo> list = ChildInfo.jsonArrayToList(array);
        DataMgr.getInstance().addChildrenInfoList(list);
    }

}
