package com.djc.logintest.constant;

public class ServerUrls {
	public static final String ROOT_RES_PATH = "kindergarten";
	public static final String PARENT_RES_PATH = "parent";
	public static final String CHILD_RES_PATH = "child";
	public static final String CLASS_RES_PATH = "class";
	public static final String SCHEDULE_RES_PATH = "schedule";
	public static final String COOKBOOK_RES_PATH = "cookbook";
	public static final String NEWS_RES_PATH = "news";
	public static final String ASSIGNMENT_RES_PATH = "assignment";
	public static final String CONVERSATION = "conversation";

	public static final String GET_TYPE_PREVIEW = "preview";
	public static final String GET_TYPE_DETAIL = "detail";
	public static final String HTTPS_HOST_ADDR = "https://www.cocobabys.com/";

	public static final String CHECK_PHONE_NUM_URL = HTTPS_HOST_ADDR
			+ "checkphonenum.do";
	// 注册时的验证码
	public static final String CHECK_REG_AUTH_CODE_URL = HTTPS_HOST_ADDR
			+ "check_reg_authcode.do";

	public static final String SEND_BIND_INFO_URL = HTTPS_HOST_ADDR
			+ "receiveBindInfo.do";
	public static final String LOGIN_URL = HTTPS_HOST_ADDR + "login.do";
	public static final String CHANGE_PWD_URL = HTTPS_HOST_ADDR
			+ "changepwd.do";
	public static final String RESET_PWD_URL = HTTPS_HOST_ADDR + "resetpwd.do";

	public static final String GET_REG_AUTHCODE = HTTPS_HOST_ADDR
			+ "get_reg_authcode.do";
	public static final String GET_RESET_PWD_AUTHCODE = HTTPS_HOST_ADDR
			+ "get_reset_pwd_authcode.do";

	public static final String LOCATION = HTTPS_HOST_ADDR + "location.do";
	public static final String CHECK_UPDATE = HTTPS_HOST_ADDR
			+ "upgrade?version=%s";
	public static final String FEED_BACK = HTTPS_HOST_ADDR + "feedback";

	private static final String DAILYLOG = "dailylog?from=%d&to=%d&most=%d";

	// https://www.cocobabys.com/kindergarten/93740362/parent/13279491366/child/1_1390238560925/dailylog?from=1390897657033&to=1390899235261&most=25
	public static final String GET_SWIPE_RECORD = HTTPS_HOST_ADDR
			+ ROOT_RES_PATH + "/%s/" + PARENT_RES_PATH + "/%s/"
			+ CHILD_RES_PATH + "/%s/" + DAILYLOG;

	// https://www.cocobabys.com/kindergarten/93740362/news?most=25&from=1389718964408&to=1389801005344
	public static final String GET_NORMAL_NOTICE = HTTPS_HOST_ADDR
			+ ROOT_RES_PATH + "/%s/" + NEWS_RES_PATH + "?";

	public static final String GET_HOMEWORK = HTTPS_HOST_ADDR + ROOT_RES_PATH
			+ "/%s/" + ASSIGNMENT_RES_PATH + "?";

	// https://www.cocobabys.com/kindergarten/93740362/conversation/123456789?from=1&to=2&most=25&sort=desc
	public static final String GET_CHAT = HTTPS_HOST_ADDR + ROOT_RES_PATH
			+ "/%s/" + CONVERSATION + "/%s" + "?";
	
	// https://www.cocobabys.com/kindergarten/93740362/conversation/123456789?from=1&to=2&most=25&sort=desc
	public static final String SEND_CHAT = HTTPS_HOST_ADDR + ROOT_RES_PATH
			+ "/%s/" + CONVERSATION + "/%s";

	// 类似kindergarten/93740362/parent/13408654680/child
	public static final String GET_ALL_CHILDREN_INFO = HTTPS_HOST_ADDR
			+ ROOT_RES_PATH + "/%s/" + PARENT_RES_PATH + "/%s/"
			+ CHILD_RES_PATH;

	public static final String UPLOAD_CHILD_INFO = HTTPS_HOST_ADDR
			+ ROOT_RES_PATH + "/%s/" + PARENT_RES_PATH + "/%s/"
			+ CHILD_RES_PATH + "/%s";

	public static final String GET_SCHOOL_PRIVIEW = HTTPS_HOST_ADDR
			+ ROOT_RES_PATH + "/%s/" + GET_TYPE_PREVIEW;

	public static final String GET_SCHOOL_DETAIL = HTTPS_HOST_ADDR
			+ ROOT_RES_PATH + "/%s/" + GET_TYPE_DETAIL;

	public static final String SCHEDULE_PRIVIEW = HTTPS_HOST_ADDR
			+ ROOT_RES_PATH + "/%s/" + CLASS_RES_PATH + "/%s/"
			+ SCHEDULE_RES_PATH + "/" + GET_TYPE_PREVIEW;

	public static final String GET_SCHEDULE = HTTPS_HOST_ADDR + ROOT_RES_PATH
			+ "/%s/" + CLASS_RES_PATH + "/%s/" + SCHEDULE_RES_PATH + "/%s";

	public static final String COOKBOOK_PRIVIEW = HTTPS_HOST_ADDR
			+ ROOT_RES_PATH + "/%s/" + COOKBOOK_RES_PATH + "/"
			+ GET_TYPE_PREVIEW;

	public static final String COOKBOOK_DETAIL = HTTPS_HOST_ADDR
			+ ROOT_RES_PATH + "/%s/" + COOKBOOK_RES_PATH + "/%s";
}
