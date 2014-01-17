package com.djc.logintest.taskmgr;

import java.util.concurrent.TimeUnit;

import com.djc.logintest.constant.EventType;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class LoadingTask extends AsyncTask<Void, Void, Void> {

    private Handler hander;

    public LoadingTask(Handler handler) {
        this.hander = handler;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        Message msg = Message.obtain();
        msg.what = EventType.LOADING_SUCCESS;
        hander.sendMessage(msg);
    }

}
