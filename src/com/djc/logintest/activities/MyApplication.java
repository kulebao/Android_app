package com.djc.logintest.activities;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.content.Intent;

import com.baidu.frontia.FrontiaApplication;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.handler.CrashHandler;
import com.djc.logintest.net.HttpsModel;
import com.djc.logintest.push.info.PushEvent;
import com.djc.logintest.service.MyService;

public class MyApplication extends FrontiaApplication {
    private static MyApplication instance;
    private static BlockingQueue<PushEvent> blockingQueue = new ArrayBlockingQueue<PushEvent>(
            ConstantValue.PUSH_ACTION_QUEUE_MAX_SIZE);

    public BlockingQueue<PushEvent> getBlockingQueue() {
        return blockingQueue;
    }

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        HttpsModel.initHttpsClient();
        instance = this;
        Intent service = new Intent(instance, MyService.class);
        instance.startService(service);
    }
}
