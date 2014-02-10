package com.djc.logintest.noticepaser;

import org.json.JSONObject;

import android.content.Context;

import com.djc.logintest.R;
import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.activities.NoticeActivity;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.Notice;

public class NormalNoticePaser implements NoticePaser {

    @Override
    public Notice saveData(JSONObject object) {
        //暂时不接收push的通告消息，改为客户端主动获取
        return null;
        // Notice notice = new Notice();
        // try {
        // Context context = MyApplication.getInstance();
        // NoticePaserHelper.setNormalParams(object, notice);
        // notice.setToClass(NoticeActivity.class);
        // String ticker =
        // context.getResources().getString(R.string.schoolnotice);
        // notice.setType(JSONConstant.NOTICE_TYPE_NORMAL);
        // notice.setTicker(ticker);
        // DataMgr.getInstance().addNotice(notice);
        // } catch (Exception e) {
        // e.printStackTrace();
        // notice = null;
        // }
        // return notice;
    }

}
