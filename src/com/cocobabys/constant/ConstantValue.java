package com.cocobabys.constant;

import android.app.Activity;

public class ConstantValue {

	public static final String TAGS_HOMEWORK = "作业";

	public static final String COMMON_SEPEARAOR = ",";

	public static final String SERVICE_COMMAND = "service_command";
	// 检查新公告
	public static final int COMMAND_TYPE_CHECK_NOTICE = 1;
	// 检查食谱
	public static final int COMMAND_TYPE_CHECK_COOKBOOK = 2;
	// 检查亲子作业
	public static final int COMMAND_TYPE_CHECK_HOMEWORK = 3;
	// 检查课程表
	public static final int COMMAND_TYPE_CHECK_SCHEDULE = 4;
	// 检查家园互动
	public static final int COMMAND_TYPE_CHECK_CHAT = 5;
	// 检查在园表现
	public static final int COMMAND_TYPE_CHECK_EDU = 6;
	// 获取绑定错误消息
	public static final int COMMAND_TYPE_SEND_BIND_ERROR = 7;

	public static final String FIRST_LOGIN = "first_login";

	public static final String CONF_INI = "conf.ini";
	public static final String UNDELETEABLE_CONFIG = "push.ini";

	public static final String HTTP_POST = "POST";
	public static final String HTTP_GET = "GET";
	public static final String API_KEY = "api_key";

	public static final String VOICE_OPEN = "1";
	public static final String VOICE_OFF = "0";
	public static final String VOICE_CONFIG = "voice_config";
	public static final String BABY_NICKNAME = "baby_nickname";
	public static final String BABY_BIRTHDAY = "baby_birthday";
	public static final String BABY_ICONURL = "baby_iconurl";
	public static final String BABY_ICONNAME = "baby_icon";

	public static final String LOCATOR_ID = "locator_id";

	public static final String LOCAL_URL = "local_url";
	public static final String DATA_ID = "data_id";
	public static final String IS_FIRST_IN = "isFirstIn";
	// 是否有新公告
	public static final String HAVE_NEWS_NOTICE = "have_news_notice";
	// 是否有新食谱
	public static final String HAVE_COOKBOOK_NOTICE = "have_cookbook_notice";
	// 是否有新亲子作业
	public static final String HAVE_HOMEWORK_NOTICE = "have_homework_notice";
	// 是否有新在园表现评价
	public static final String HAVE_EDUCATION_NOTICE = "have_education_notice";
	// 是否有新留言
	public static final String HAVE_CHAT_NOTICE = "have_chat_notice";

	// 是否有新课程表
	public static final String HAVE_SCHEDULE_NOTICE = "have_schedule_notice";

	public static final String SCHOOL_INFO_TIMESTAMP = "school_info_timestamp";

	public static final String ENTER = "\n";

	public static final int PUSH_ACTION_QUEUE_MAX_SIZE = 100;

	public static final int TIME_LIMIT_TO_GET_AUTHCODE_AGAIN = 60 * 2;

	public static final int DO_NOT_CANCEL_DIALOG = 100;

	public static final int TYPE_GET_REG_AUTHCODE = 1;
	public static final int TYPE_GET_RESER_PWD_AUTHCODE = 2;
	public static final String SWIPE_DATE = "swipe_date";
	public static final String NOTICE_TYPE = "notice_type";
	public static final String LATEST_CHECK_NEW_TIME = "check_new_time";
	public static final String LATEST_CHECK_NOTICE_TIME = "check_notice_time";
	// 检查更新间隔时间为48小时
	public static final long CHECK_NEW_TIME_SPAN = 24 * 60 * 60 * 1000L;

	// 检查新通知间隔时间5小时
	public static final long CHECK_NOTICE_TIME_SPAN = 5 * 60 * 60 * 1000L;

	public static final String TMP_CHAT_PATH = "tmp_chat_path";

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
	public static final int SEND_CHAT_SUCCESS = Activity.RESULT_OK;
	public static final int SEND_CHAT_FAIL = 21;

	public static final String HEADER_TOKEN = "token";
	public static final String HEADER_SOURCE = "source";
	public static final String SOURCE_ANDROID = "android";

	public static final int GET_NORMAL_NOTICE_MAX_COUNT = 25;
	public static final int GET_EDU_MAX_COUNT = 25;

	public static final int GET_CHATINFO_MAX_COUNT = 25;
	public static final int GET_HOMEWORK_MAX_COUNT = 25;
	// 下拉获取
	public static final int TYPE_GET_HEAD = 0;
	// 上拉获取
	public static final int TYPE_GET_TAIL = 1;
	// 测试是否有新公告，不存数据库
	public static final int TYPE_CHECK_NEW = 2;

	public static final String TEST_PHONE = "13408654680";
	public static final String TEST_PHONE_PWD = "zzzzzz";

	// 缩略图的像素限制(长或宽)
	public static final int MINI_PIC_SIZE = 80;

	// 小孩头像像素限制，主界面上显示的
	public static final int BABY_HEAD_PIC_SIZE = 320;

	// 列表界面，显示头像
	public static final int HEAD_ICON_HEIGHT = 50;
	public static final int HEAD_ICON_WIDTH = 50;

	public static final int NAIL_ICON_WIDTH = 70;
	public static final int NAIL_ICON_HEIGHT = 70;

	public static final String EXP_YEAR = "exp_year";
	public static final String EXP_MONTH = "exp_month";

	public static final String EXP_ID = "exp_id";

	public static final int MAX_SELECT_LIMIT = 9;

	public static final String FAKE_CHANNEL_ID = "fake_channel_id";

	public static final String FAKE_USER_ID = "fake_user_id";

	public static final String BIND_ERROR = "bind_error";

	public static final String DEFAULT_VOICE_TYPE = ".amr";

	public static final String RECENTLY_PIC_DIR = "recently_pic_dir";

	public static final String RECORD_FILE_NAME = "record_file_name";

	public static final String IS_PUBLIC_VIDEO = "is_public_video";

	public static final String ACTION_DETAIL = "action_detail";

	public static final String MERCHANT_DETAIL = "merchant_detail";

	public static final String BUSINESS_STATE = "business_state";
	public static final String BUSINESS_VISIBLE = "business_visible";

	public static final String BUSINESS_INFO = "business_info";

	public static final String RELATION_INFO = "relation_info";

	public static final String VERSION_CODE = "version_code";

	public static final String UPGRADE = "upgrade";
	
	public static final String IM_TOKEN = "im_token";
	
	public static final String SHOW_GROUP_ENTRY = "show_group_entry";
	
	public static final String CLASS_ID = "class_id";
	
	public static final String IM_GROUP_ID = "im_group_id";
	public static final String IM_GROUP_NAME = "im_group_name";

	// 亲子摄影 0001
	public static final int MERCHANT_CATEGORY_CAMERA = 1;
	// 其他 0000
	public static final int MERCHANT_CATEGORY_OTHER = 0;
	// 教育 0010
	public static final int MERCHANT_CATEGORY_EDUCATION = 2;
	// 游乐 0100
	public static final int MERCHANT_CATEGORY_GAME = 4;
	// 购物 1000
	public static final int MERCHANT_CATEGORY_SHOPPING = 8;

	// 活动图片最大宽度
	public static final int ACTION_PIC_MAX_WIDTH = 800;
	// 活动图片最大高度
	public static final int ACTION_PIC_MAX_HEIGHT = 640;

}
