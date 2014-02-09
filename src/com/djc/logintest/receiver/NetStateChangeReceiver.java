package com.djc.logintest.receiver;

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
        }
    }

}
