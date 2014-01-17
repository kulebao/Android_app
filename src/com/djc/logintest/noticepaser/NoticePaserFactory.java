package com.djc.logintest.noticepaser;

import com.djc.logintest.constant.JSONConstant;

public class NoticePaserFactory {
    private NoticePaserFactory() {
    }

    public static NoticePaser createNoticePaser(int type) {
        NoticePaser noticePaser = null;
        switch (type) {
        case JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKIN:
            //2种类型一种处理流程
        case JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKOUT:
            noticePaser = new SwapCardNoticePaser();
            break;
        case JSONConstant.NOTICE_TYPE_COOKBOOK:
            noticePaser = new CookBookNoticePaser();
            break;
        case JSONConstant.NOTICE_TYPE_NORMAL:
            noticePaser = new NormalNoticePaser();
            break;
        case JSONConstant.NOTICE_TYPE_STUDY:
            noticePaser = new StudyNoticePaser();
            break;
        case JSONConstant.NOTICE_TYPE_CHILD_STATUS:
            noticePaser = new ChildStatusNoticePaser();
            break;
        default:
            break;
        }
        return noticePaser;
    }
}
