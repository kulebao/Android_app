package com.cocobabys.taskmgr;

import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;

public class AuthCodeCountDownTask extends AsyncTask<Void, Void, Void> {

    private Handler hander;
    // 倒计时时长，秒为单位
    private int timeLimit;

    public AuthCodeCountDownTask(Handler handler, int timeLimit) {
        this.hander = handler;
        this.timeLimit = timeLimit;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            for (int i = timeLimit; i >= 0; i--) {
                TimeUnit.SECONDS.sleep(1);
                Message msg = Message.obtain();
                msg.what = EventType.AUTHCODE_COUNTDOWN_GO;
                msg.arg2 = ConstantValue.DO_NOT_CANCEL_DIALOG;
                msg.arg1 = i;
                hander.sendMessage(msg);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        Message msg = Message.obtain();
        msg.what = EventType.AUTHCODE_COUNTDOWN_OVER;
        msg.arg2 = ConstantValue.DO_NOT_CANCEL_DIALOG;
        hander.sendMessage(msg);
    }

}
