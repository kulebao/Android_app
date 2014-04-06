package com.cocobabys.noticepaser;

import org.json.JSONException;
import org.json.JSONObject;

import com.cocobabys.constant.JSONConstant;
import com.cocobabys.dbmgr.info.Notice;
import com.cocobabys.utils.Utils;

public class NoticePaserHelper {

    public static void setNormalParams(JSONObject object, Notice notice) throws JSONException {
        String title = object.getString(JSONConstant.NOTIFICATION_TITLE);
        long timestamp = Long.valueOf(object.getString(JSONConstant.TIME_STAMP));
        String body = object.getString(JSONConstant.NOTIFICATION_BODY);
        String publisher = object.getString(JSONConstant.PUBLISHER);
        notice.setContent(body);
        notice.setPublisher(publisher);
        notice.setTitle(title);
        notice.setTimestamp(Utils.convertTime(timestamp));
    }

}
