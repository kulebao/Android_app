package com.djc.logintest.net;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.constant.ServerUrls;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.Notice;
import com.djc.logintest.httpclientmgr.HttpClientHelper;
import com.djc.logintest.noticepaser.NoticePaserHelper;
import com.djc.logintest.utils.Utils;

public class GetNormalNoticeMethod {

    private static final String MOST = "most";
    private static final String FROM = "from";
    private static final String TO = "to";

    private GetNormalNoticeMethod() {
    }

    public static GetNormalNoticeMethod getMethod() {
        return new GetNormalNoticeMethod();
    }

    public List<Notice> getNormalNotice(int most, long from, long to) throws Exception {
        List<Notice> list = new ArrayList<Notice>();
        HttpResult result = new HttpResult();
        String command = createNormalNoticeCommand(most, from, to);
        Log.e("DDDDD ", "getSchoolInfo cmd:" + command);
        result = HttpClientHelper.executeGet(command);
        list = handleCheckSchoolInfoResult(result);
        return list;
    }

    private List<Notice> handleCheckSchoolInfoResult(HttpResult result) throws JSONException {
        List<Notice> list = new ArrayList<Notice>();
        if (result.getResCode() == HttpStatus.SC_OK) {
            JSONArray array = result.getJSONArray();

            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                Notice notice = parseNotice(object);
                // NoticePaserHelper.setNormalParams(object, notice);
                list.add(notice);
            }
        }

        return list;
    }

    private Notice parseNotice(JSONObject object) throws JSONException {
        Notice notice = new Notice();
        String title = object.getString("title");
        Long timestamp = Long.parseLong(object.getString(JSONConstant.TIME_STAMP));
        String body = object.getString("content");
        notice.setContent(body);
        notice.setPublisher("测试djc");
        notice.setTitle(title);
        notice.setTimestamp(Utils.convertTime(timestamp));
        // notice.setTimestamp(timestamp);
        notice.setType(JSONConstant.NOTICE_TYPE_NORMAL);
        return notice;
    }

    private String createNormalNoticeCommand(int most, long from, long to) {
        if (most == 0) {
            most = ConstantValue.GET_NORMAL_NOTICE_MAX_COUNT;
        }
        String cmd = String.format(ServerUrls.GET_NORMAL_NOTICE, DataMgr.getInstance()
                .getSchoolID());

        cmd += MOST + "=" + most;
        if (from != 0) {
            cmd += "&" + FROM + "=" + from;
        }

        if (to != 0) {
            cmd += "&" + TO + "=" + to;
        }
        Log.d("DDD", "createNormalNoticeCommand cmd=" + cmd);
        return cmd;
    }
}
