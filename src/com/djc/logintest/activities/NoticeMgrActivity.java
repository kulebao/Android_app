package com.djc.logintest.activities;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.djc.logintest.R;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.Notice;

public class NoticeMgrActivity extends TabChildActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_mgr);
        initView();
        // test();
    }

    private void test() {
        DataMgr instance = DataMgr.getInstance();
        List<Notice> allNotice = instance.getAllNotice();
        if (allNotice.size() < 10) {
            for (int i = 0; i < 400; i++) {
                Notice info = new Notice();
                info.setTitle("测试Title" + i);
                info.setContent("测试Body，这里是通知测试内容，请大家查看，哈哈！！！" + i);
                info.setTimestamp("2013-10-27 10:22:66");
                info.setType(JSONConstant.NOTICE_TYPE_OTHER);
                if (i % 2 == 0) {
                    info.setType(JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKIN);
                }
                instance.addNotice(info);
            }
        }
    }

    private void initView() {
        Button showSwipCardNoticeBtn = (Button) findViewById(R.id.showSwipCardNoticeView);

        showSwipCardNoticeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startToNoticeRecordActivity(JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKIN);
            }
        });

        Button showOtherNoticeBtn = (Button) findViewById(R.id.showOtherNoticeView);

        showOtherNoticeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startToNoticeRecordActivity(JSONConstant.NOTICE_TYPE_COOKBOOK);
            }
        });
    }

    private void startToNoticeRecordActivity(int noticeTypeSwipecard) {
        Intent intent = new Intent();
        intent.putExtra(ConstantValue.NOTICE_TYPE, noticeTypeSwipecard);
        intent.setClass(this, NoticeRecordActivity.class);
        startActivity(intent);
    }
}
