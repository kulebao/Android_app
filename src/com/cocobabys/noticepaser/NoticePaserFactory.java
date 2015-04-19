package com.cocobabys.noticepaser;

import com.cocobabys.constant.JSONConstant;

public class NoticePaserFactory {
	private NoticePaserFactory() {
	}

	public static NoticePaser createNoticePaser(int type) {
		NoticePaser noticePaser = null;
		switch (type) {
		case JSONConstant.NOTICE_TYPE_AFTERNOON_GET_ON:
		case JSONConstant.NOTICE_TYPE_AFTERNOON_GET_OFF:
		case JSONConstant.NOTICE_TYPE_MORNING_GET_OFF:
		case JSONConstant.NOTICE_TYPE_MORNING_GET_ON:
		case JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKIN:
			// 2种类型一种处理流程
		case JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKOUT:
			noticePaser = new SwapCardNoticePaser();
			break;
		case JSONConstant.NOTICE_TYPE_NORMAL:
			noticePaser = new NormalNoticePaser();
			break;
		default:
			break;
		}
		return noticePaser;
	}
}
