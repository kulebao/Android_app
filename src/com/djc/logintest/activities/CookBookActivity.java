package com.djc.logintest.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;

import com.djc.logintest.R;
import com.djc.logintest.adapter.CookBookListAdapter;
import com.djc.logintest.bean.CookbookItem;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.CookBookInfo;
import com.djc.logintest.handler.MyHandler;
import com.djc.logintest.taskmgr.GetCookbookTask;
import com.djc.logintest.utils.Utils;

public class CookBookActivity extends Activity {
    private CookBookListAdapter adapter;
    private ListView list;
    private Handler handler;
    private List<CookbookItem> cookbookItems = new ArrayList<CookbookItem>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cook_list);
        ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.cookbook_notice);
        initHandler();
        initData();
        runGetCookBookTask();
    }

    private void initHandler() {
        handler = new MyHandler(this, null) {
            @Override
            public void handleMessage(Message msg) {
                if (CookBookActivity.this.isFinishing()) {
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                super.handleMessage(msg);
                switch (msg.what) {
                case EventType.GET_COOKBOOK_SUCCESS:
            		// 获取到新的食谱，将新食谱的标志置为false
            		Utils.saveProp(ConstantValue.HAVE_COOKBOOK_NOTICE, "false");
                    initData();
                    break;
                // 已是最新
                case EventType.GET_COOKBOOK_LATEST:
                	Utils.saveProp(ConstantValue.HAVE_COOKBOOK_NOTICE, "false");
                    break;
                default:
                    break;
                }
            }
        };
    }

    private void initData() {
        CookBookInfo cookBookInfo = DataMgr.getInstance().getCookBookInfo();
        if (cookBookInfo != null) {
            cookbookItems.clear();
            cookbookItems = cookBookInfo.getCookbookItemList();
            if (!cookbookItems.isEmpty()) {
                adapter = new CookBookListAdapter(this, cookbookItems);
                list = (ListView) findViewById(R.id.cook_list);
                list.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void runGetCookBookTask() {
        new GetCookbookTask(handler).execute();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("DDD JJJ", "ScheduleActivity onNewIntent");
        initData();
    }
}
