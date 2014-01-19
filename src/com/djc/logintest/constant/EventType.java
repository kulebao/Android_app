package com.djc.logintest.constant;

public class EventType {
    // 数值不要随意修改，与服务器返回值对应
    public static final int PHONE_NUM_IS_INVALID = 1100;
    public static final int PHONE_NUM_IS_FIRST_USE = 1101;
    public static final int PHONE_NUM_IS_ALREADY_BIND = 1102;

    public static final int AUTH_CODE_IS_VALID = 1130;
    public static final int AUTH_CODE_IS_INVALID = 1131;

    public static final int GET_AUTH_CODE_SUCCESS = 1140;
    public static final int GET_AUTH_CODE_FAIL = 1141;

    public static final int NET_WORK_INVALID = 1201;
    public static final int AUTHCODE_COUNTDOWN_GO = 1210;
    public static final int AUTHCODE_COUNTDOWN_OVER = 1211;
    public static final int SERVER_INNER_ERROR = 1221;
    public static final int SERVER_BUSY = 1225;

    public static final int PHONE_NUM_INPUT_ERROR = 1301;
    public static final int AUTH_CODE_INPUT_ERROR = 1302;

    public static int PWD_FORMAT_ERROR = 1310;
    public static int OLD_PWD_FORMAT_ERROR = 1311;
    public static int NEW_PWD_FORMAT_ERROR = 1312;

    public static final int CHANGE_PWD_SUCCESS = 1320;
    public static final int OLD_PWD_NOT_EQUAL = 1322;

    public static final int LOADING_SUCCESS = 1400;
    public static final int RESET_PWD_SUCCESS = 1410;
    public static final int RESET_PWD_FAILED = 1411;

    public static final int BIND_SUCCESS = 1500;
    public static final int BIND_FAILED = 1501;
    
    public static final int UPLOAD_SUCCESS = 1505;
    public static final int UPLOAD_FAILED = 1506;

    public static final int GET_COOKBOOK_FAILED = 1525;
    public static final int GET_COOKBOOK_SUCCESS = 1526;
    public static final int GET_COOKBOOK_LATEST = 1527;
    
    public static final int GET_NOTICE_SUCCESS = 1540;
    public static final int GET_NOTICE_FAILED = 1541;
    
    
    public static final int LOGIN_SUCCESS = 1600;
    public static final int PWD_INCORRECT = 1601;

    public static final int UPDATE_SCHOOL_INFO = 1610;
    public static final int SCHOOL_INFO_IS_LATEST = 1611;
    public static final int GET_SCHOOL_INFO_FAILED = 1612;

    public static final int UPDATE_CHILDREN_INFO = 1620;
    public static final int CHILDREN_INFO_IS_LATEST = 1621;
    public static final int GET_CHILDREN_INFO_FAILED = 1622;
    
    public static final int GET_SCHEDULE_FAILED = 1625;
    public static final int GET_SCHEDULE_SUCCESS = 1626;
    public static final int GET_SCHEDULE_LATEST = 1627;
    
    
    public static final int DOWNLOAD_IMG_SUCCESS = 1630;
    public static final int DOWNLOAD_IMG_FAILED = 1631;

    public static final int HAS_NEW_VERSION = 1700;
    public static final int HAS_NO_VERSION = 1701;

}
