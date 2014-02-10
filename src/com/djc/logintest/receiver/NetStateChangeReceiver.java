package com.djc.logintest.receiver;

import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.service.MyService;
import com.djc.logintest.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetStateChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Utils.isNetworkConnected(context)) {
            Log.d("DDD", "onReceive NetStateChangeReceiver");
            Intent myintent = new Intent(context, MyService.class);
            myintent.putExtra(ConstantValue.COMMAND_CHECK_NOTICE,
                    ConstantValue.COMMAND_TYPE_CHECK_NOTICE);
            context.startService(myintent);
        }
    }

}
