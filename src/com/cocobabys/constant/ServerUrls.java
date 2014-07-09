package com.cocobabys.constant;

import com.cocobabys.activities.MyApplication;

public class ServerUrls {
	public static final String ROOT_RES_PATH = "kindergarten";
	public static final String PARENT_RES_PATH = "parent";
	public static final String EMPLOYEE_RES_PATH = "employee";
	public static final String CHILD_RES_PATH = "child";
	public static final String CLASS_RES_PATH = "class";
	public static final String SCHEDULE_RES_PATH = "schedule";
	public static final String COOKBOOK_RES_PATH = "cookbook";
	public static final String NEWS_RES_PATH = "news";
	public static final String EDU_RES_PATH = "assess";
	public static final String ASSIGNMENT_RES_PATH = "assignment";
	public static final String CONVERSATION = "conversation";
	public static final String RELATIONSHIP = "relationship";

	public static final String GET_TYPE_PREVIEW = "preview";
	public static final String GET_TYPE_DETAIL = "detail";
	public static final String HTTPS_HOST_ADDR = "https://www.cocobabys.com/";
	public static final String CHAT_SESSION = "session";
	public static final String SENDER = "sender";
	public static final String RECORD_RES_PATH = "record";
	public static final String EXP_RES_PATH = "history";
	public static final String STATISTICS_RES_PATH = "statistics";

	public static final String TEST_HTTPS_HOST_ADDR = "https://stage2.cocobabys.com/";

	private static String getHost() {
		String host = HTTPS_HOST_ADDR;
		if (MyApplication.getInstance().isForTest()) {
			// if (Utils.isTestHost()) {
			host = TEST_HTTPS_HOST_ADDR;
			// }
		}
		return host;
	}

	public static final String CHECK_PHONE_NUM_URL = getHost()
			+ "checkphonenum.do";
	// 注册时的验证码
	public static final String CHECK_REG_AUTH_CODE_URL = getHost()
			+ "check_reg_authcode.do";

	public static final String SEND_BIND_INFO_URL = getHost()
			+ "receiveBindInfo.do";
	public static final String LOGIN_URL = getHost() + "login.do";
	public static final String CHANGE_PWD_URL = getHost() + "changepwd.do";
	public static final String RESET_PWD_URL = getHost() + "resetpwd.do";

	public static final String GET_REG_AUTHCODE = getHost()
			+ "get_reg_authcode.do";
	public static final String GET_RESET_PWD_AUTHCODE = getHost()
			+ "get_reset_pwd_authcode.do";

	public static final String LOCATION = getHost() + "location.do";
	public static final String CHECK_UPDATE = getHost() + "upgrade?version=%s";
	public static final String FEED_BACK = getHost() + "feedback";

	private static final String DAILYLOG = "dailylog?from=%d&to=%d&most=%d";

	// https://www.cocobabys.com/ws/verify/phone/13227882592
	public static final String GET_AUTH_CODE_URL = getHost()
			+ "ws/verify/phone/%s";

	// https://www.cocobabys.com/kindergarten/93740362/parent/13279491366/child/1_1390238560925/dailylog?from=1390897657033&to=1390899235261&most=25
	public static final String GET_SWIPE_RECORD = getHost() + ROOT_RES_PATH
			+ "/%s/" + CHILD_RES_PATH + "/%s/" + DAILYLOG;

	// https://www.cocobabys.com/kindergarten/93740362/news?most=25&from=1389718964408&to=1389801005344
	public static final String GET_NORMAL_NOTICE = getHost() + ROOT_RES_PATH
			+ "/%s/" + NEWS_RES_PATH + "?";

	// https://www.cocobabys.com/kindergarten/93740362/child/1_93740362_456/assess?from=1&to=10&most=5
	public static final String GET_EDUCATION = getHost() + ROOT_RES_PATH
			+ "/%s/" + CHILD_RES_PATH + "/%s/" + EDU_RES_PATH + "?";

	public static final String GET_HOMEWORK = getHost() + ROOT_RES_PATH
			+ "/%s/" + ASSIGNMENT_RES_PATH + "?";

	// https://www.cocobabys.com/kindergarten/93740362/conversation/123456789?from=1&to=2&most=25&sort=desc
	public static final String GET_CHAT = getHost() + ROOT_RES_PATH + "/%s/"
			+ CONVERSATION + "/%s" + "?";

	// https://stage2.cocobabys.com/kindergarten/1003/session/1_1396844597394/record
	public static final String GET_NEW_CHAT = getHost() + ROOT_RES_PATH
			+ "/%s/" + CHAT_SESSION + "/%s/" + RECORD_RES_PATH + "?";

	// https://stage2.cocobabys.com/kindergarten/1003/session/1_1396844597394
	public static final String SEND_NEW_CHAT = getHost() + ROOT_RES_PATH
			+ "/%s/" + CHAT_SESSION + "/%s/" + RECORD_RES_PATH;

	// https://www.cocobabys.com/kindergarten/93740362/conversation/123456789?from=1&to=2&most=25&sort=desc
	public static final String SEND_CHAT = getHost() + ROOT_RES_PATH + "/%s/"
			+ CONVERSATION + "/%s";

	// 类似kindergarten/93740362/parent/13408654680/child
	public static final String GET_ALL_CHILDREN_INFO = getHost()
			+ ROOT_RES_PATH + "/%s/" + PARENT_RES_PATH + "/%s/"
			+ CHILD_RES_PATH;

	// https://stage2.cocobabys.com/kindergarten/1003/relationship?parent=13408654680
	public static final String GET_RELATIONSHIP = getHost() + ROOT_RES_PATH
			+ "/%s/" + RELATIONSHIP + "?";

	// 类似kindergarten/93740362/parent/13408654680/child
	public static final String GET_TEACHER_INFO = getHost() + ROOT_RES_PATH
			+ "/%s/" + EMPLOYEE_RES_PATH + "?phone=%s";

	// https://stage2.cocobabys.com/kindergarten/1003/sender/3_1003_1399268817590?type=t
	public static final String GET_SENDER_INFO = getHost() + ROOT_RES_PATH
			+ "/%s/" + SENDER + "/%s?";

	// https://www.cocobabys.com/kindergarten/93740362/relationship?parent=13408654680
	// public static final String GET_ALL_CHILDREN_INFO = getHost()
	// + ROOT_RES_PATH + "/%s/" + RELATIONSHIP + "?parent=%s";

	public static final String UPLOAD_CHILD_INFO = getHost() + ROOT_RES_PATH
			+ "/%s/" + CHILD_RES_PATH + "/%s";

	public static final String GET_SCHOOL_PRIVIEW = getHost() + ROOT_RES_PATH
			+ "/%s/" + GET_TYPE_PREVIEW;

	// https://cocobabys.com/ws/fileToken?bucket=cocobabys&key=ddd/djc/2.jpg
	public static final String GET_UPLOAD_TOKEN = "https://cocobabys.com/ws/fileToken?bucket="
			+ "%s";

	public static final String GET_SCHOOL_DETAIL = getHost() + ROOT_RES_PATH
			+ "/%s/";

	public static final String SCHEDULE_PRIVIEW = getHost() + ROOT_RES_PATH
			+ "/%s/" + CLASS_RES_PATH + "/%s/" + SCHEDULE_RES_PATH + "/"
			+ GET_TYPE_PREVIEW;

	public static final String GET_SCHEDULE = getHost() + ROOT_RES_PATH
			+ "/%s/" + CLASS_RES_PATH + "/%s/" + SCHEDULE_RES_PATH + "/%s";

	public static final String COOKBOOK_PRIVIEW = getHost() + ROOT_RES_PATH
			+ "/%s/" + COOKBOOK_RES_PATH + "/" + GET_TYPE_PREVIEW;

	public static final String COOKBOOK_DETAIL = getHost() + ROOT_RES_PATH
			+ "/%s/" + COOKBOOK_RES_PATH + "/%s";

	public static final String GET_EXP_COUNT = getHost() + ROOT_RES_PATH
			+ "/%s/" + EXP_RES_PATH + "/%s/" + STATISTICS_RES_PATH + "?";

	public static final String GET_EXP_INFO = getHost() + ROOT_RES_PATH
			+ "/%s/" + EXP_RES_PATH + "/%s/" + RECORD_RES_PATH + "?";

	public static final String POST_EXP_INFO = getHost() + ROOT_RES_PATH
			+ "/%s/" + EXP_RES_PATH + "/%s/" + RECORD_RES_PATH;

	public static final String DELETE_CHAT = getHost() + ROOT_RES_PATH + "/%s/"
			+ CHAT_SESSION + "/%s/" + RECORD_RES_PATH + "/%d";
}
