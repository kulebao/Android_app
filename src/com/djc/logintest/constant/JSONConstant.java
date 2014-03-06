package com.djc.logintest.constant;

//不要修改下面属性的值，因为是与服务器端一一对应的
public class JSONConstant {
    public static final String PASSWORD = "password";
    public static final String OLD_PASSWORD = "old_password";
    public static final String NEW_PASSWORD = "new_password";

    public static final String ERROR_CODE = "error_code";
    public static final String PUSH_TAGS = "push_tags";

    public static final String SCHOOL_NAME = "school_name";
    public static final String CHILD_NAME = "child_name";
    public static final String CHILD_PIC_URL = "child_pic_url";
    // 用户姓名,ini文件的key
    public static final String USERNAME = "username";
    // 用户帐号名，也就是手机号码，ini文件的key
    public static final String ACCOUNT_NAME = "account_name";
    // token，ini文件的key
    public static final String ACCESS_TOKEN = "access_token";
    public static final String PHONE_NUM = "phonenum";
    public static final String AUTH_CODE = "authcode";
    public static final String SCHOOL_ID = "school_id";
    // public static final String CLASS_ID = "class_id";

    public static final String UPDATE_URL = "url";
    public static final String UPDATE_CONTENT = "summary";
    public static final String UPDATE_VERSION_CODE = "update_version_code";
    public static final String UPDATE_VERSION_NAME = "version";
    public static final String FILE_SIZE = "size";

    public static final int NOTICE_TYPE_SWIPECARD_CHECKOUT = 0;
    public static final int NOTICE_TYPE_SWIPECARD_CHECKIN = 1;
    public static final int NOTICE_TYPE_NORMAL = 2;
    public static final int NOTICE_TYPE_LOCATION = 6;
    public static final int NOTICE_TYPE_OTHER = 11;
    public static final int CHECK_NEW_COMMAND = 51;

    public static final String NOTIFICATION_ID = "notice_id";
    public static final String NOTIFICATION_TYPE = "notice_type";
    public static final String NOTIFICATION_INFO = "notice_info";
    public static final String NOTIFICATION_CUSTOM = "custom";
    public static final String NOTIFICATION_TITLE = "notice_title";
    public static final String NOTIFICATION_BODY = "notice_body";
    public static final String PUBLISHER = "publisher";
    
    public static final String NET_URL = "net_url";
    public static final String LOCAL_URL = "local_url";

    // 设备的pushid，服务器用来进行消息推送,以下常量定义的值与百度服务器匹配
    public static final String USER_ID = "user_id";
    public static final String CHANNEL_ID = "channel_id";
    public static final String DEVICE_TYPE = "android";
    
    public static final String RESPONSE_PARAMS = "response_params";
    public static final String TAGS_DETAIL = "details";
    public static final String TAGS_RESULT = "result";
    public static final String TAG = "tag";

    // 经度
    public static final String LONGITUDE = "longitude";
    // 纬度
    public static final String LATITUDE = "latitude";
    // 定位设备id
    public static final String DEVICE_ID = "device_id";
    // 具体地点说明，比如某市某街某号
    public static final String ADDRESS = "address";
    public static final String TIME_STAMP = "timestamp";
    // push消息里面，自定义字段与通用字段区别
    public static final String PUSH_CUSTOM = "custom";
    // 定位器手机号码
    public static final String LBS_NUM = "lbs_num";

    public static final String COOK_DATE = "cook_date";
    public static final String COOK_WEEK = "cook_week";
    public static final String COOK_BREAKFIRST = "cook_breakfirst";
    public static final String COOK_LUNCH = "cook_lunch";
    public static final String COOK_DINNER = "cook_dinner";

    // 学校信息的json字符串，包括学校logo地址，学校描述，学校联系电话,学校信息版本
    public static final String SCHOOL_INFO = "school_info";

}
