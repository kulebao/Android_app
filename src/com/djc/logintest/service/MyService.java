package com.djc.logintest.service;

import com.baidu.android.pushservice.PushSettings;
import com.djc.logintest.handler.PushEventHandler;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("DJC 10-16", "MyService onCreate");
        PushSettings.enableDebugMode(this, true);        
        PushEventHandler.getPushEventHandler().start();
    }

}
