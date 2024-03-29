package com.cocobabys.constant;

import com.cocobabys.activities.MyApplication;

public class ServerUrls {
	// old path "receiveBindInfo.do";
	private static final String BIND_PATH = "api/v1/binding";
	public static final String LOCATION_PATH = "location";
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
	public static final String VIDEO_MEMBER = "video_member";
	public static final String AD = "ad";
	public static final String LAST_BUS_LOCATION = "last_bus_location";
	public static final String CONTRACTOR = "contractor";
	public static final String ACTIVITY = "activity";
	public static final String ENROLLMENT = "enrollment";

	public static final String INVITATION = "invitation";

	public static final String COMMERCIAL_SUMMARY = "commercial_summary";

	public static final String CLASS_IM_GROUP = "class_im_group";

	public static final String SHARE = "share";

	public static final String FIX_PATH = "api/v2/";

	public static final String DYNAMIC_PATH = "api/v3/";

	public static final String IM_TOKEN = "im_token";

	public static final String MANAGER = "manager";

	public static final String USER = "user";

	public static final String TEST_HTTPS_HOST_ADDR = "https://stage2.cocobabys.com/";

	public static String getHost() {
		String host = HTTPS_HOST_ADDR;
		if (MyApplication.getInstance().isForTest()) {
			// if (Utils.isTestHost()) {
			host = TEST_HTTPS_HOST_ADDR;
			// }
		}
		return host;
	}

	public static final String CHECK_PHONE_NUM_URL = getHost() + "checkphonenum.do";
	// 注册时的验证码
	public static final String CHECK_REG_AUTH_CODE_URL = getHost() + "check_reg_authcode.do";

	public static final String SEND_BIND_INFO_URL = getHost() + BIND_PATH;

	public static final String LOGIN_URL = getHost() + "login.do";
	public static final String CHANGE_PWD_URL = getHost() + "changepwd.do";
	public static final String RESET_PWD_URL = getHost() + "resetpwd.do";

	public static final String GET_REG_AUTHCODE = getHost() + "get_reg_authcode.do";
	public static final String GET_RESET_PWD_AUTHCODE = getHost() + "get_reset_pwd_authcode.do";

	public static final String LOCATION = getHost() + "location.do";

	public static final String CHECK_UPDATE = getHost() + "upgrade?version=%s";
	public static final String FEED_BACK = getHost() + "feedback";

	private static final String DAILYLOG = "dailylog?from=%d&to=%d&most=%d";

	// https://www.cocobabys.com/ws/verify/phone/13227882592
	public static final String GET_AUTH_CODE_URL = getHost() + "ws/verify/phone/%s";

	// https://www.cocobabys.com/kindergarten/93740362/parent/13279491366/child/1_1390238560925/dailylog?from=1390897657033&to=1390899235261&most=25
	public static final String GET_SWIPE_RECORD = getHost() + ROOT_RES_PATH + "/%s/" + CHILD_RES_PATH + "/%s/"
			+ DAILYLOG;

	// https://www.cocobabys.com/api/v2/kindergarten/93740362/news?most=25&from=1389718964408&to=1389801005344
	public static final String GET_NORMAL_NOTICE = getHost() + ROOT_RES_PATH + "/%s/" + NEWS_RES_PATH + "?";

	// https://www.cocobabys.com/kindergarten/93740362/news?most=25&from=1389718964408&to=1389801005344
	public static final String GET_NOTICE_WITH_TAGS = getHost() + FIX_PATH + ROOT_RES_PATH + "/%s/" + NEWS_RES_PATH
			+ "?";

	// https://www.cocobabys.com/kindergarten/93740362/child/1_93740362_456/assess?from=1&to=10&most=5
	public static final String GET_EDUCATION = getHost() + ROOT_RES_PATH + "/%s/" + CHILD_RES_PATH + "/%s/"
			+ EDU_RES_PATH + "?";

	public static final String GET_HOMEWORK = getHost() + ROOT_RES_PATH + "/%s/" + ASSIGNMENT_RES_PATH + "?";

	// https://www.cocobabys.com/kindergarten/93740362/conversation/123456789?from=1&to=2&most=25&sort=desc
	public static final String GET_CHAT = getHost() + ROOT_RES_PATH + "/%s/" + CONVERSATION + "/%s" + "?";

	// https://stage2.cocobabys.com/kindergarten/1003/session/1_1396844597394/record
	public static final String GET_NEW_CHAT = getHost() + ROOT_RES_PATH + "/%s/" + CHAT_SESSION + "/%s/"
			+ RECORD_RES_PATH + "?";

	// https://stage2.cocobabys.com/kindergarten/1003/session/1_1396844597394
	public static final String SEND_NEW_CHAT = getHost() + ROOT_RES_PATH + "/%s/" + CHAT_SESSION + "/%s/"
			+ RECORD_RES_PATH;

	// https://www.cocobabys.com/kindergarten/93740362/conversation/123456789?from=1&to=2&most=25&sort=desc
	public static final String SEND_CHAT = getHost() + ROOT_RES_PATH + "/%s/" + CONVERSATION + "/%s";

	// 类似kindergarten/93740362/parent/13408654680/child
	public static final String GET_ALL_CHILDREN_INFO = getHost() + ROOT_RES_PATH + "/%s/" + PARENT_RES_PATH + "/%s/"
			+ CHILD_RES_PATH;

	// https://stage2.cocobabys.com/kindergarten/1003/relationship?parent=13408654680
	public static final String GET_RELATIONSHIP = getHost() + ROOT_RES_PATH + "/%s/" + RELATIONSHIP + "?";

	// https://stage2.cocobabys.com/kindergarten/8901/relationship?class_id=20001
	public static final String GET_CLASS_RELATIONSHIP = getHost() + ROOT_RES_PATH + "/%s/" + RELATIONSHIP + "?";

	// https://stage2.cocobabys.com/kindergarten/1003/relationship?child=1
	public static final String GET_RELATIONSHIP_BY_CHILD = getHost() + ROOT_RES_PATH + "/%s/" + RELATIONSHIP + "?";

	// 类似kindergarten/93740362/employee/?phone=
	public static final String GET_TEACHER_INFO = getHost() + ROOT_RES_PATH + "/%s/" + EMPLOYEE_RES_PATH + "?phone=%s";

	// 类似/kindergarten/:kg/class/:classId/manager
	public static final String GET_TEACHER_LIST = getHost() + ROOT_RES_PATH + "/%s/" + CLASS_RES_PATH + "/%s/"
			+ MANAGER;

	// https://stage2.cocobabys.com/kindergarten/1003/sender/3_1003_1399268817590?type=t
	public static final String GET_SENDER_INFO = getHost() + ROOT_RES_PATH + "/%s/" + SENDER + "/%s?";

	// https://www.cocobabys.com/kindergarten/93740362/relationship?parent=13408654680
	// public static final String GET_ALL_CHILDREN_INFO = getHost()
	// + ROOT_RES_PATH + "/%s/" + RELATIONSHIP + "?parent=%s";

	public static final String UPLOAD_CHILD_INFO = getHost() + ROOT_RES_PATH + "/%s/" + CHILD_RES_PATH + "/%s";

	public static final String GET_SCHOOL_PRIVIEW = getHost() + ROOT_RES_PATH + "/%s/" + GET_TYPE_PREVIEW;

	// https://cocobabys.com/ws/fileToken?bucket=cocobabys&key=ddd/djc/2.jpg
	public static final String GET_UPLOAD_TOKEN = "https://cocobabys.com/ws/fileToken?bucket=" + "%s";

	public static final String GET_SCHOOL_DETAIL = getHost() + ROOT_RES_PATH + "/%s/";

	public static final String SCHEDULE_PRIVIEW = getHost() + ROOT_RES_PATH + "/%s/" + CLASS_RES_PATH + "/%s/"
			+ SCHEDULE_RES_PATH + "/" + GET_TYPE_PREVIEW;

	public static final String GET_SCHEDULE = getHost() + ROOT_RES_PATH + "/%s/" + CLASS_RES_PATH + "/%s/"
			+ SCHEDULE_RES_PATH + "/%s";

	public static final String COOKBOOK_PRIVIEW = getHost() + ROOT_RES_PATH + "/%s/" + COOKBOOK_RES_PATH + "/"
			+ GET_TYPE_PREVIEW;

	public static final String COOKBOOK_DETAIL = getHost() + ROOT_RES_PATH + "/%s/" + COOKBOOK_RES_PATH + "/%s";

	public static final String GET_EXP_COUNT = getHost() + ROOT_RES_PATH + "/%s/" + EXP_RES_PATH + "/%s/"
			+ STATISTICS_RES_PATH + "?";

	public static final String GET_EXP_INFO = getHost() + ROOT_RES_PATH + "/%s/" + EXP_RES_PATH + "/%s/"
			+ RECORD_RES_PATH + "?";

	public static final String POST_EXP_INFO = getHost() + ROOT_RES_PATH + "/%s/" + EXP_RES_PATH + "/%s/"
			+ RECORD_RES_PATH;

	public static final String DELETE_CHAT = getHost() + ROOT_RES_PATH + "/%s/" + CHAT_SESSION + "/%s/"
			+ RECORD_RES_PATH + "/%d";

	// https://stage2.cocobabys.com/kindergarten/2088/history/2_2088_896/record/583
	public static final String DELETE_EXP = getHost() + ROOT_RES_PATH + "/%s/" + EXP_RES_PATH + "/%s/" + RECORD_RES_PATH
			+ "/%d";

	// https://stage2.cocobabys.com/api/v1/kindergarten/2088/video_member/222
	public static final String GET_VIDEO_INFO = getHost() + "api/v1/" + ROOT_RES_PATH + "/%s/" + VIDEO_MEMBER + "/%s";

	// https://stage2.cocobabys.com/api/v1/kindergarten/2088/ad
	public static final String GET_AD_INFO = getHost() + "api/v1/" + ROOT_RES_PATH + "/%s/" + AD;

	// https://stage2.cocobabys.com/api/v2/location/1451351909/record?most=1
	public static final String GET_LAST_LOCATION = getHost() + "api/v2/" + LOCATION_PATH + "/%s/" + "record?most=1";

	// https://stage2.cocobabys.com/api/v2/location/1451351909/record
	public static final String GET_HISTORY_LOCATION = getHost() + "api/v2/" + LOCATION_PATH + "/%s/" + "record";

	// https://stage2.cocobabys.com/api/v2/location/1451351909/status/power
	public static final String GET_LOCATOR_POWER = getHost() + "api/v2/" + LOCATION_PATH + "/%s/" + "status/power";

	public static final String GET_SCHOOL_CONFIG = getHost() + "api/v2/school_config/%s";

	// https://stage2.cocobabys.com/api/v1/kindergarten/2088/video_member/default
	public static final String GET_PUBLIC_VIDEO_INFO = getHost() + "api/v1/kindergarten/%s/video_member/default";

	// https://stage2.cocobabys.com/api/v2/kindergarten/2088/news/123/reader
	public static final String POST_NEWS_RECEIPT = getHost() + "api/v2/kindergarten/%s/" + NEWS_RES_PATH + "/%d/reader";

	public static final String GET_RECEIPT_STATE = getHost() + "api/v2/kindergarten/%s/" + NEWS_RES_PATH
			+ "/%d/reader/%s";

	// https://stage2.cocobabys.com/api/v2/kindergarten/2088/last_bus_location/2_2088_897
	public static final String GET_SCHOOLBUS_LAST_LOCATION = getHost() + "api/v2/kindergarten/%s/" + LAST_BUS_LOCATION
			+ "/%s";

	// https://stage2.cocobabys.com/api/v4/kindergarten/93740362/commercial_summary
	public static final String GET_BUSINESS_STATE = getHost() + "api/v4/kindergarten/%s/" + COMMERCIAL_SUMMARY;

	// https://stage2.cocobabys.com/api/v4/kindergarten/2088/contractor
	public static final String GET_MERCHANT_LIST = getHost() + "api/v4/kindergarten/%s/" + CONTRACTOR;

	// https://stage2.cocobabys.com/api/v4/kindergarten/2088/contractor/2/activity
	public static final String GET_MERCHANT_ACTION_LIST = getHost() + "api/v4/kindergarten/%s/" + CONTRACTOR + "/%d/"
			+ ACTIVITY;

	// https://stage2.cocobabys.com/api/v4/kindergarten/2088/activity
	public static final String GET_ACTION_LIST = getHost() + "api/v4/kindergarten/%s/" + ACTIVITY;

	// https://stage2.cocobabys.com/api/v4/kindergarten/2088/activity/:activityId/parent/:parentId/enrollment
	public static final String GET_ENROLL_STATE = getHost() + "api/v4/kindergarten/%s/" + ACTIVITY + "/%d/"
			+ PARENT_RES_PATH + "/%s/" + ENROLLMENT;

	// https://stage2.cocobabys.com/api/v4/kindergarten/2088/activity/:activityId/enrollment
	public static final String DO_ENROLL = getHost() + "api/v4/kindergarten/%s/" + ACTIVITY + "/%d/" + ENROLLMENT;

	// 获取分享成长经历的token
	public static final String EXP_SHARE_URL = getHost() + "api/v3/kindergarten/%s/" + EXP_RES_PATH + "/%s/"
			+ RECORD_RES_PATH + "/%s/" + SHARE;

	// 真正的分享链接
	public static final String EXP_SHARE_REAL_URL = getHost() + "s" + "/%s";

	// /api/v5/kindergarten/:schoolId/invitation
	public static final String INVITE_URL = getHost() + "api/v5/" + ROOT_RES_PATH + "/%s/" + INVITATION;

	// /kindergarten/:kg/relationship/:card
	public static final String BIND_CARD_URL = getHost() + ROOT_RES_PATH + "/%s/" + RELATIONSHIP + "/%s";

	// https://cocobabys.com/kindergarten/2041/parent/12222333344
	public static final String UPDATE_PARENT_URL = getHost() + ROOT_RES_PATH + "/%s/" + PARENT_RES_PATH + "/%s";

	// https://cocobabys.com//api/v5/invitation_code/:phone
	public static final String INVITE_CODE_URL = getHost() + "api/v5/invitation_code/%s";

	// https://cocobabys.com/api/v7/kindergarten/:kg/class_im_group/:class_id
	// CLASS_IM_GROUP
	public static final String GET_GROUP_INFO_URL = getHost() + "api/v7/" + ROOT_RES_PATH + "/%s/" + CLASS_IM_GROUP
			+ "/%s";
	// https://cocobabys.com/api/v7/kindergarten/:kg/class_im_group/:class_id/user
	// CLASS_IM_GROUP
	public static final String JOIN_GROUP_INFO_URL = getHost() + "api/v7/" + ROOT_RES_PATH + "/%s/" + CLASS_IM_GROUP
			+ "/%s/" + USER;

	// https://stage2.cocobabys.com/api/v7/kindergarten/8901/im_token/p_8901_Some(9029)_12323232323
	public static final String REFRESH_IM_TOKEN_URL = getHost() + "api/v7/" + ROOT_RES_PATH + "/%s/" + IM_TOKEN + "/%s";
}
