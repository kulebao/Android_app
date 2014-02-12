package com.djc.logintest.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.net.GetCookbookMethod;
import com.djc.logintest.utils.Utils;

public class GetCookbookTask extends AsyncTask<Void, Void, Integer> {

    private Handler handler;

    public GetCookbookTask(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int result = EventType.NET_WORK_INVALID;
        boolean networkConnected = Utils.isNetworkConnected(MyApplication.getInstance());
        if (networkConnected) {
            GetCookbookMethod method = GetCookbookMethod.getMethod();
            result = method.getCookBook();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Message msg = Message.obtain();
        msg.what = result;
        handler.sendMessage(msg);
    }
}
