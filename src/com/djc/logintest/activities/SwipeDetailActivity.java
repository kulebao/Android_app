package com.djc.logintest.activities;

import java.sql.Timestamp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.djc.logintest.R;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.Notice;
import com.djc.logintest.noticepaser.SwapCardNoticePaser;
import com.djc.logintest.utils.Utils;

public class SwipeDetailActivity extends Activity {
    private TextView contentView;
    private ImageView noticeiconView;
    private TextView signView;
    private TextView timeView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice);
        Log.d("DDD JJJ", "NoticeActivity onCreate");
        initView();
        setData(getIntent());
    }

    private void setData(Intent intent) {
        int id = intent.getIntExtra(JSONConstant.NOTIFICATION_ID, -1);
        Notice noticeByID = DataMgr.getInstance().getNoticeByID(id);
        if (noticeByID != null) {
            Bitmap bmp = Utils.getLoacalBitmap(SwapCardNoticePaser.createSwipeIconPath(String
                    .valueOf(id)));
            if (bmp != null) {
                noticeiconView.setVisibility(View.VISIBLE);
                Utils.setImg(noticeiconView, bmp);
            }

            signView.setText(noticeByID.getPublisher());
            timeView.setText(noticeByID.getTimestamp());

            contentView.setText(noticeByID.getTitle() + "\n" + "   " + noticeByID.getContent());
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
