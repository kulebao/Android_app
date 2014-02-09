package com.djc.logintest.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.djc.logintest.R;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.SwipeInfo;
import com.djc.logintest.handler.MyHandler;
import com.djc.logintest.noticepaser.SwapCardNoticePaser;
import com.djc.logintest.taskmgr.DownLoadImgAndSaveTask;
import com.djc.logintest.utils.Utils;

public class SwipeDetailActivity extends Activity {
    private TextView contentView;
    private ImageView noticeiconView;
    private TextView signView;
    private TextView timeView;
    private Handler handler;
    private SwipeInfo swipeinfo;
    private AsyncTask<Void, Void, Integer> downloadIconTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice);
        Log.d("DDD JJJ", "NoticeActivity onCreate");
        initHandler();
        initView();
        setData(getIntent());
    }

    private void initHandler() {
        handler = new MyHandler(this, null) {
            @Override
            public void handleMessage(Message msg) {
                if (SwipeDetailActivity.this.isFinishing()) {
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                super.handleMessage(msg);
                switch (msg.what) {
                case EventType.DOWNLOAD_IMG_SUCCESS:
                    setIcon();
                    break;
                default:
                    break;
                }
            }

        };
    }

    private void setData(Intent intent) {
        int id = intent.getIntExtra(JSONConstant.NOTIFICATION_ID, -1);
        swipeinfo = DataMgr.getInstance().getSwipeDataByID(id);
        try {
            if (swipeinfo != null) {
                setIcon();
                setPublisher();
                setTimestamp();
                setTitle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setIcon() {
        Bitmap bmp = Utils.getLoacalBitmap(SwapCardNoticePaser.createSwipeIconPath(String
                .valueOf(swipeinfo.getTimestamp())));
        if (bmp != null) {
            noticeiconView.setVisibility(View.VISIBLE);
            Utils.setImg(noticeiconView, bmp);
        } else {
            runDownloadIconTask();
        }
    }

    private void runDownloadIconTask() {
        if (downloadIconTask != null && downloadIconTask.getStatus() == AsyncTask.Status.RUNNING) {
            // 后执行的取消先执行的
            downloadIconTask.cancel(true);
        }

        downloadIconTask = new DownLoadImgAndSaveTask(handler, swipeinfo.getUrl(),
                SwapCardNoticePaser.createSwipeIconPath(String.valueOf(swipeinfo.getTimestamp())))
                .execute();
    }

    public void setTitle() {
        contentView.setText(swipeinfo.getNoticeTitle());
    }

    public void setTimestamp() {
        timeView.setText(swipeinfo.getFormattedTime());
    }

    public void setPublisher() {
        try {
            signView.setText(DataMgr.getInstance().getSchoolInfo().getSchool_name());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        signView = (TextView) findViewById(R.id.sign);
        timeView = (TextView) findViewById(R.id.time);
        contentView = (TextView) findViewById(R.id.noticecontent);
        noticeiconView = (ImageView) findViewById(R.id.noticeicon);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("DDD JJJ", "NoticeActivity onNewIntent");
        setIntent(intent);
        setData(intent);
    }

}
