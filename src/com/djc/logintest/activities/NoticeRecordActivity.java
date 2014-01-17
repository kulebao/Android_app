package com.djc.logintest.activities;

import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.djc.logintest.R;
import com.djc.logintest.adapter.NoticeListAdapter;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.Notice;
import com.djc.logintest.utils.Utils;

public class NoticeRecordActivity extends Activity {

    private ListView list;
    private NoticeListAdapter adapter;
    private int titleID;
    private int noticeType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_list);
        getParam();
        ActivityHelper.setBackKeyLitsenerOnTopbar(this, titleID);
        initListAdapter();
    }

    private void getParam() {
        noticeType = getIntent().getIntExtra(ConstantValue.NOTICE_TYPE,
                JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKIN);
        if (noticeType == JSONConstant.NOTICE_TYPE_SWIPECARD_CHECKIN) {
            titleID = R.string.swipcard_record;
        } else if (noticeType == JSONConstant.NOTICE_TYPE_NORMAL) {
            titleID = R.string.schoolnotice;
        } else {
            titleID = R.string.noticeTitle;
        }

    }

    private void initListAdapter() {
        List<Notice> listinfo = DataMgr.getInstance().getAllNoticeByType(noticeType);
        adapter = new NoticeListAdapter(this, listinfo);
        list = (ListView) findViewById(R.id.notice_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Notice info = (Notice) adapter.getItem(position);
                startTo(info);
            }
        });
    }

    private void startTo(Notice info) {
        Class<?> toClass = NoticeActivity.class;
        if (info.getType() == JSONConstant.NOTICE_TYPE_COOKBOOK) {
            toClass = CookBookActivity.class;
        }
        Intent intent = new Intent(this, toClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtra(JSONConstant.NOTIFICATION_TITLE, info.getTitle());
        intent.putExtra(JSONConstant.NOTIFICATION_BODY, info.getContent());
        intent.putExtra(JSONConstant.TIME_STAMP, info.getTimestamp());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == Menu.FIRST) {
            Utils.showTwoBtnResDlg(R.string.delete_all_notice_confirm, this, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DataMgr.getInstance().removeAllNoticeByType(noticeType);
                    adapter.clear();
                }
            });
        }
        return true;
    }
}