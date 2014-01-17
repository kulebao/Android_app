package com.djc.logintest.dbmgr.info;

public class LocationInfo {
    public static final String ID = "_id";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String TIMESTAMP = "timestamp";
    public static final String LBS_NUM = "lbs_num";
    public static final String ADDRESS = "address";

    private String longitude = "";
    private String latitude = "";
    private String address = "";
    // 定位器手机号码
    private String lbs_num = "";
    private int id = 0;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String timestamp = "";

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLbs_num() {
        return lbs_num;
    }

    public void setLbs_num(String lbs_num) {
        this.lbs_num = lbs_num;
    }

}
