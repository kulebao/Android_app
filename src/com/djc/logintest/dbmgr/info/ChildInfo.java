package com.djc.logintest.dbmgr.info;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChildInfo {
    public static final int STATUS_SELECTED = 1;
    public static final int STATUS_UNSELECTED = 0;

    public static final String ID = "_id";
    public static final String CHILD_NICK_NAME = "child_nick_name";
    public static final String CHILD_LOCAL_HEAD_ICON = "local_url";
    public static final String CHILD_SERVER_HEAD_ICON = "server_url";
    public static final String CHILD_BIRTHDAY = "child_birthday";
    // 有多位小孩的情况下，标示当前被选中的小孩
    public static final String SELECTED = "selected";
    // 服务器上小孩id，restful定位小孩资源
    public static final String SERVER_ID = "server_id";
    public static final String CLASS_ID = "class_id";
    public static final String CLASS_NAME = "class_name";

    private String child_nick_name = "";
    private String local_url = "";
    private String server_url = "";
    private String child_birthday = "";
    // 0表示未选中， 1表示选中，只能有一条记录为1
    private int selected = STATUS_UNSELECTED;
    private String timestamp = "";
    private String server_id = "";
    private String class_id = "";
    private String class_name = "";

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public String getClass_id() {
        return class_id;
    }

    public void setClass_id(String class_id) {
        this.class_id = class_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getServer_id() {
        return server_id;
    }

    public void setServer_id(String server_id) {
        this.server_id = server_id;
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    private int id = 0;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChild_nick_name() {
        return child_nick_name;
    }

    public void setChild_nick_name(String child_nick_name) {
        this.child_nick_name = child_nick_name;
    }

    public String getLocal_url() {
        return local_url;
    }

    public void setLocal_url(String local_url) {
        this.local_url = local_url;
    }

    public String getServer_url() {
        return server_url;
    }

    public void setServer_url(String server_url) {
        this.server_url = server_url;
    }

    public String getChild_birthday() {
        return child_birthday;
    }

    public void setChild_birthday(String child_birthday) {
        this.child_birthday = child_birthday;
    }

    public static List<ChildInfo> jsonArrayToList(JSONArray array) {
        List<ChildInfo> list = new ArrayList<ChildInfo>();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject obj = array.getJSONObject(i);
                ChildInfo info = jsonObjToChildInfo(obj);
                // 简单的把第一个小孩设置为选中
                if (i == 0) {
                    info.setSelected(STATUS_SELECTED);
                }
                list.add(info);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    public static ChildInfo jsonObjToChildInfo(JSONObject obj) throws JSONException {
        ChildInfo info = new ChildInfo();
        info.setServer_id(obj.getString("id"));
        info.setChild_nick_name(obj.getString("nick"));
        info.setServer_url(obj.getString("icon_url"));
        info.setChild_birthday(obj.getString("birthday"));
        info.setTimestamp(obj.getString("timestamp"));
        try {
            info.setClass_id(obj.getString("class_id"));
            info.setClass_name(obj.getString("class_name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }
}
