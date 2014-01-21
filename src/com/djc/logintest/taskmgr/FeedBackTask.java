package com.djc.logintest.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.net.FeedBackMethod;
import com.djc.logintest.utils.Utils;

public class FeedBackTask extends AsyncTask<Void, Void, Integer> {
    private Handler handler;
    private String content;

    public FeedBackTask(Handler handler, String content) {
        this.handler = handler;
        this.content = content;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int bret = EventType.UPLOAD_FAILED;
        boolean networkConnected = Utils.isNetworkConnected(MyApplication.getInstance());
        if(networkConnected){
            bret = FeedBackMethod.getMethod().feedBack(content);
        }
        return bret;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Message msg = Message.obtain();
        msg.what = result;
        handler.sendMessage(msg);
    }

}
