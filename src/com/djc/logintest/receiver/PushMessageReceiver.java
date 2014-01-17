package com.djc.logintest.receiver;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.djc.logintest.handler.PushEventHandler;
import com.djc.logintest.push.info.PushEvent;
import com.djc.logintest.service.MyService;
import com.djc.logintest.utils.Utils;

/**
 * Push消息处理receiver
 */
public class PushMessageReceiver extends BroadcastReceiver {
    public static final String TAG = PushMessageReceiver.class.getSimpleName();

    AlertDialog.Builder builder;

    /**
     * @param context
     *            Context
     * @param intent
     *            接收的intent
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d("DDD", ">>> Receive intent: \r\n" + intent);
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            // 开机启动后台服务
            Intent service = new Intent(context, MyService.class);
            context.startService(service);
            return;
        }
        
        IntentPaser paser = IntentPaser.getIntentPaser();
        PushEvent event = paser.paraseIntent(context, intent);
        if(event != null){
            PushEventHandler.getPushEventHandler().offerEvent(event);
        }
    }

}
