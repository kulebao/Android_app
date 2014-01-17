package com.djc.logintest.activities;

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
import com.djc.logintest.noticepaser.SwapCardNoticePaser;
import com.djc.logintest.utils.Utils;

public class NoticeActivity extends Activity {
    private TextView contentView;
    private ImageView noticeiconView;
    private TextView signView;
    private TextView timeView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice);
        ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.notice_title);
        Log.d("DDD JJJ", "NoticeActivity onCreate");
        initView();
        setData(getIntent());
    }

    private void setData(Intent intent) {
        String title = intent.getStringExtra(JSONConstant.NOTIFICATION_TITLE);
        String content = intent.getStringExtra(JSONConstant.NOTIFICATION_BODY);

        String time = intent.getStringExtra(JSONConstant.TIME_STAMP);
        String publisher = intent.getStringExtra(JSONConstant.PUBLISHER);

        signView.setText(publisher);
        timeView.setText(time);

        contentView.setText(title + "\n" + "   " + content);
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
