package com.djc.logintest.dbmgr.info;

import org.json.JSONException;
import org.json.JSONObject;

import com.djc.logintest.constant.JSONConstant;

public class SchoolInfo {
    public static final String ID = "_id";
    // 分配给每个学校的唯一标识
    public static final String SCHOOL_ID = "school_id";
    public static final String SCHOOL_NAME = "school_name";
    public static final String SCHOOL_PHONE = "school_phone";
    public static final String SCHOOL_DESC = "school_desc";
    public static final String SCHOOL_LOCAL_URL = "school_local_url";
    public static final String SCHOOL_SERVER_URL = "school_server_url";
    // 幼儿园介绍，幼儿园管理人员可以在后台更新
    private String school_logo_local_url = "";
    private String school_logo_server_url = "";
    private String school_desc = "";
    private String school_phone = "";
    private String school_id = "";
    private String school_name = "";
    private String timestamp = "";
    private int shcool_info_version = 0;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSchool_id() {
        return school_id;
    }

    public void setSchool_id(String school_id) {
        this.school_id = school_id;
    }

    public String getSchool_name() {
        return school_name;
    }

    public void setSchool_name(String school_name) {
        this.school_name = school_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int id = 0;

    public String getSchool_desc() {
        return school_desc;
    }

    public void setSchool_desc(String school_desc) {
        this.school_desc = school_desc;
    }

    public String getSchool_phone() {
        return school_phone;
    }

    public void setSchool_phone(String school_phone) {
        this.school_phone = school_phone;
    }

    public String getSchool_logo_local_url() {
        return school_logo_local_url;
    }

    public void setSchool_logo_local_url(String school_logo_local_url) {
        this.school_logo_local_url = school_logo_local_url;
    }

    public String getSchool_logo_server_url() {
        return school_logo_server_url;
    }

    public void setSchool_logo_server_url(String school_logo_server_url) {
        this.school_logo_server_url = school_logo_server_url;
    }

    public int getShcool_info_version() {
        return shcool_info_version;
    }

    public void setShcool_info_version(int shcool_info_version) {
        this.shcool_info_version = shcool_info_version;
    }

    public static SchoolInfo jsonObjToChildInfo(JSONObject obj) throws JSONException {
        SchoolInfo info = new SchoolInfo();
        info.setSchool_phone(obj.getString("phone"));
        info.setSchool_desc(obj.getString("desc"));
        info.setSchool_logo_server_url(obj.getString("school_logo_url"));
        info.setTimestamp(obj.getString("timestamp"));
        info.setSchool_name(obj.getString("name"));
        info.setSchool_id(obj.getString(JSONConstant.SCHOOL_ID));
        return info;
    }
}
