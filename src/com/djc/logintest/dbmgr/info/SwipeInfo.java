package com.djc.logintest.dbmgr.info;

import org.json.JSONException;
import org.json.JSONObject;

import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.utils.Utils;

public class SwipeInfo {
    private static final String SWIPE_CARD_TITLE = "尊敬的用户 %s 您好:";

    private static final String SWIPE_CARD_IN_BODY = "您的小孩 %s 已于%s刷卡入园!";
    private static final String SWIPE_CARD_OUT_BODY = "您的小孩 %s 已于%s刷卡离园!";

    public static final String ID = "_id";
    public static final String TIMESTAMP = "timestamp";
    public static final String TYPE = "type";
    public static final String CHILD_ID = "child_id";
    public static final String ICON_URL = "icon_url";

    private long timestamp = 0;

    private int type = 0;
    private int id = 0;
    private String child_id = "";
    private String url = "";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getChild_id() {
        return child_id;
    }

    public void setChild_id(String child_id) {
        this.child_id = child_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFormattedTime() {
        String ret = "";
        try {
            ret = Utils.convertTime(timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public String getNoticeBody(String nickname) {
        String sample = (type == JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKIN ? SWIPE_CARD_IN_BODY
                : SWIPE_CARD_OUT_BODY);

        String body = String.format(sample, nickname, getFormattedTime());
        return body;
    }
    public String getNoticeTitle() {
        String title = String.format(SWIPE_CARD_TITLE, Utils.getProp(JSONConstant.USERNAME));
        return title;
    }
    
    public static SwipeInfo toSwipeInfo(JSONObject obj) throws JSONException{
        int type = obj.getInt(JSONConstant.NOTIFICATION_TYPE);
        final String record_url = obj.getString("record_url");
        String child_id = obj.getString("child_id");
        long timestamp = Long.valueOf(obj.getString(JSONConstant.TIME_STAMP));
        SwipeInfo info = new SwipeInfo();
        info.setChild_id(child_id);
        info.setType(type);
        info.setUrl(record_url);
        info.setTimestamp(timestamp);
        return info;
    }

}
