package com.djc.logintest.handler;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.djc.logintest.R;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.utils.Utils;

public class MyHandler extends Handler {

    private Dialog dialog;
    private Activity activity;

    public MyHandler(Activity activity) {
        this(activity, null);
    }

    public MyHandler(Activity activity, Dialog dialog) {
        this.activity = activity;
        this.dialog = dialog;
    }

    @Override
    public void handleMessage(Message msg) {
        // if (activity.isFinishing()) {
        // Log.w("djc", "do nothing when activity finishing!");
        // return;
        // }

        if (msg.arg2 != ConstantValue.DO_NOT_CANCEL_DIALOG && dialog != null) {
            dialog.cancel();
        }

        switch (msg.what) {
        case EventType.NET_WORK_INVALID:
            Toast.makeText(activity, R.string.net_error, Toast.LENGTH_SHORT).show();
            break;
        case EventType.SERVER_INNER_ERROR:
            Toast.makeText(activity, R.string.server_error, Toast.LENGTH_SHORT).show();
            break;
        case EventType.SERVER_BUSY:
            Toast.makeText(activity, R.string.server_error, Toast.LENGTH_SHORT).show();
            break;
        case EventType.AUTH_CODE_IS_INVALID:
            Utils.showSingleBtnEventDlg(EventType.AUTH_CODE_IS_INVALID, activity);
            break;
        default:
            break;
        }
    }

}
