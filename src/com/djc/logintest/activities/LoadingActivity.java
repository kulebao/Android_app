package com.djc.logintest.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.R;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.taskmgr.LoadingTask;
import com.djc.logintest.utils.Utils;

public class LoadingActivity extends Activity {
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        initHandler();
        new LoadingTask(handler).execute();
    }

    private void initHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case EventType.LOADING_SUCCESS:
                    if(Utils.isFirstStart()){
                        startGuardActivity();
                    }
                    else{
                        goNextActivity();
                    }
                    break;

                default:
                    break;
                }
            }
        };
    }
    
    private void startGuardActivity(){
        Intent intent = new Intent();
        intent.setClass(this, GuideActivity.class);
        startActivity(intent);
        finish();  
    }

    private void goNextActivity() {
        Class<?> toClass = null;
        if (Utils.isLoginout()) {
            // 用户未绑定
            toClass = ValidatePhoneNumActivity.class;
        } else {
            toClass = MainActivity.class;
        }

        Intent intent = new Intent();
        intent.setClass(this, toClass);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        return;
    }

}
