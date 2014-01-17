package com.djc.logintest.activities;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.djc.logintest.R;
import com.djc.logintest.adapter.NoticeListAdapter;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.customview.MsgListView;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.Notice;
import com.djc.logintest.handler.MyHandler;
import com.djc.logintest.taskmgr.GetNoticeTask;

public class NoticePullRefreshActivity extends Activity {

    private NoticeListAdapter adapter;
    private MsgListView msgListView;
    private View footer;
    private Handler myhandler;
    private AsyncTask<Void, Void, Void> getNoticeTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_pull_refresh_list);
        ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.schoolnotice);
        initHander();
        initCustomListView();
    }

    private boolean runGetNoticeTask() {
        boolean bret = true;
        if (getNoticeTask == null || getNoticeTask.getStatus() != AsyncTask.Status.RUNNING) {
            getNoticeTask = new GetNoticeTask(myhandler).execute();
        } else {
            bret = false;
            Log.d("djc", "should not getNewsImpl task already running!");
        }
        return bret;
    }

    private void initHander() {
        myhandler = new MyHandler(this, null) {
            @Override
            public void handleMessage(Message msg) {
                msgListView.onRefreshComplete();
                switch (msg.what) {
                case EventType.GET_NOTICE_SUCCESS:
                    Toast.makeText(NoticePullRefreshActivity.this, "get suceess!",
                            Toast.LENGTH_SHORT).show();

                    footer.setVisibility(View.GONE);
                    break;
                default:
                    break;
                }
            }
        };
    }

    private void initCustomListView() {
        List<Notice> listinfo = DataMgr.getInstance().getAllNoticeByType(
                JSONConstant.NOTICE_TYPE_NORMAL);
        adapter = new NoticeListAdapter(this, listinfo);
        msgListView = (MsgListView) findViewById(R.id.noticelist);// 继承ListActivity，id要写成android.R.id.list，否则报异常
        msgListView
                .setonRefreshListener(new com.djc.logintest.customview.MsgListView.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        boolean runtask = runGetNoticeTask();
                        if (!runtask) {
                            // 任务没有执行，立即去掉下拉显示
                            msgListView.onRefreshComplete();
                        }
                    }
                });

        msgListView.setAdapter(adapter);
        msgListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 自定义listview headview占了一个，所以真实数据从1开始
                int currentIndex = position - 1;
                if (currentIndex >= adapter.getCount()) {
                    // 当底部条出现时，index会大于count造成数组越界异常，这里处理一下
                    return;
                }
                Notice info = (Notice) adapter.getItem(currentIndex);
                startTo(info);
            }
        });
        msgListView.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    // 判断是否滚动到底部
                    if (view.getLastVisiblePosition() == view.getCount() - 1) {
                        Log.d("djc", "on the end!!!!!!!!!!!!!!!!");
                        boolean runtask = runGetNoticeTask();
                        if (runtask) {
                            footer.setVisibility(View.VISIBLE);
                        }

                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {
            }
        });
        addFooter();
    }

    public void addFooter() {
        footer = getLayoutInflater().inflate(R.layout.footerview, null);
        msgListView.addFooterView(footer);
    }

    private void startTo(Notice info) {
        Intent intent = new Intent(this, NoticeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(JSONConstant.NOTIFICATION_TITLE, info.getTitle());
        intent.putExtra(JSONConstant.NOTIFICATION_BODY, info.getContent());
        intent.putExtra(JSONConstant.TIME_STAMP, info.getTimestamp());
        startActivity(intent);
    }
}