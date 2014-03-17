package com.djc.logintest.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;

import com.djc.logintest.R;
import com.djc.logintest.adapter.ScheduleListAdapter;
import com.djc.logintest.bean.ScheduleListItem;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.ScheduleInfo;
import com.djc.logintest.handler.MyHandler;
import com.djc.logintest.taskmgr.GetScheduleTask;
import com.djc.logintest.utils.Utils;

public class ScheduleActivity extends UmengStatisticsActivity {
    private ScheduleListAdapter adapter;
    private ListView list;
    private Handler handler;
    private List<ScheduleListItem> scheduleListItemList = new ArrayList<ScheduleListItem>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_list);
        ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.schedule);
        initHandler();
        initData();
        runGetScheduleTask();
    }

    private void initHandler() {
        handler = new MyHandler(this, null) {
            @Override
            public void handleMessage(Message msg) {
                if (ScheduleActivity.this.isFinishing()) {
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                super.handleMessage(msg);
                switch (msg.what) {
                case EventType.GET_SCHEDULE_SUCCESS:
                	Utils.saveProp(ConstantValue.HAVE_SCHEDULE_NOTICE, "false");
                    initData();
                    break;
                // 已是最新
                case EventType.GET_SCHEDULE_LATEST:
					Utils.saveProp(ConstantValue.HAVE_SCHEDULE_NOTICE, "false");
                    break;
                default:
                    break;
                }
            }
        };
    }

    private void initData() {
        ScheduleInfo scheduleInfo = DataMgr.getInstance().getScheduleInfo();
        if (scheduleInfo != null) {
            scheduleListItemList.clear();
            scheduleListItemList = scheduleInfo.getScheduleListItemList();
            if (!scheduleListItemList.isEmpty()) {
                adapter = new ScheduleListAdapter(this, scheduleListItemList);
                list = (ListView) findViewById(R.id.schedule_list);
                list.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void runGetScheduleTask() {
        new GetScheduleTask(handler).execute();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("DDD JJJ", "ScheduleActivity onNewIntent");
        initData();
    }

}
