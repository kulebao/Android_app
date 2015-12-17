package com.cocobabys.taskmgr;

import java.util.concurrent.TimeUnit;

import com.cocobabys.constant.EventType;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class GetNoticeTask extends AsyncTask<Void, Void, Void> {

    private Handler hander;

    public GetNoticeTask(Handler handler) {
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
        msg.what = EventType.GET_NOTICE_SUCCESS;
        hander.sendMessage(msg);
    }

}
