package com.djc.logintest.constant;

public class ConstantValue {

    public static final String FIRST_LOGIN = "first_login";

    public static final String CONF_INI = "conf.ini";
    public static final String PUSH_CONFIG = "push.ini";

    public static final String HTTP_POST = "POST";
    public static final String HTTP_GET = "GET";
    public static final String API_KEY = "api_key";
    
    public static final String VOICE_OPEN = "1";
    public static final String VOICE_OFF = "0";
    public static final String VOICE_CONFIG = "voice_config";
    public static final String BABY_NICKNAME = "baby_nickname";
    public static final String BABY_BIRTHDAY = "baby_birthday";
    public static final String BABY_ICONURL= "baby_iconurl";
    public static final String BABY_ICONNAME = "baby_icon";
    
    public static final String IS_FIRST_IN = "isFirstIn";
    
    public static final String SCHOOL_INFO_TIMESTAMP = "school_info_timestamp";
    
    public static final String ENTER = "\n";

    public static final int PUSH_ACTION_QUEUE_MAX_SIZE = 100;

    public static final int TIME_LIMIT_TO_GET_AUTHCODE_AGAIN = 60;

    public static final int DO_NOT_CANCEL_DIALOG = 100;

    public static final int TYPE_GET_REG_AUTHCODE = 1;
    public static final int TYPE_GET_RESER_PWD_AUTHCODE = 2;
    public static final String SWIPE_DATE = "swipe_date";
    public static final String NOTICE_TYPE = "notice_type";
    public static final String LATEST_CHECK_NEW_TIME = "check_new_time";
    // 检查更新间隔时间为720小时
    public static final long CHECK_NEW_TIME_SPAN = 720 * 60 * 60 * 1000L;

    // 以下是百度服务需要的，不要修改值
    public static final String EXTRA_MESSAGE = "message";

    public static final String ACTION_SHOW_MESSAGE = "bccsclient.action.SHOW_MESSAGE";

    public static final String ACTION_RESPONSE = "bccsclient.action.RESPONSE";

    public static final String ACTION_MESSAGE = "com.baiud.pushdemo.action.MESSAGE";

    protected static final String ACTION_LOGIN = "com.baidu.pushdemo.action.LOGIN";

    public static final String RESPONSE_ERRCODE = "errcode";

    public static final String RESPONSE_CONTENT = "content";

    public static final String RESPONSE_METHOD = "method";

    public static final int START_SETTING = 100;
    public static final int EXIT_LOGIN_RESULT = 10;

    public static final String HEADER_TOKEN = "token";

	public static final int GET_NORMAL_NOTICE_MAX_COUNT = 25;

}
