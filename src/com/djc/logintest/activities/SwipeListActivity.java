package com.djc.logintest.activities;

import java.util.List;

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
import com.djc.logintest.adapter.SwipeListAdapter;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.SwipeInfo;
import com.djc.logintest.utils.Utils;

public class SwipeListActivity extends UmengStatisticsActivity {
    private ListView list;
    private SwipeListAdapter adapter;
    private String date;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_list);
        getParam();
        ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.swipcard_record);
        initListAdapter();
    }

    private void getParam() {
        date = getIntent().getStringExtra(ConstantValue.SWIPE_DATE);
    }

    private void initListAdapter() {
        List<SwipeInfo> listinfo = DataMgr.getInstance().getAllSwipeCardNotice(date);
        adapter = new SwipeListAdapter(this, listinfo);
        list = (ListView) findViewById(R.id.notice_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SwipeInfo info = (SwipeInfo) adapter.getItem(position);
                startToSwipeDetailActivity(info);
            }
        });
    }

    private void startToSwipeDetailActivity(SwipeInfo info) {
        Intent intent = new Intent(this, SwipeDetailActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(JSONConstant.NOTIFICATION_ID, info.getId());
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
                }
            });
        }
        return true;
    }
}