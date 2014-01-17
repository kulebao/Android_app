package com.djc.logintest.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.net.GetScheduleMethod;
import com.djc.logintest.utils.Utils;

public class GetScheduleTask extends AsyncTask<Void, Void, Integer> {

    private Handler handler;

    public GetScheduleTask(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int result = EventType.NET_WORK_INVALID;
        boolean networkConnected = Utils.isNetworkConnected(MyApplication.getInstance());
        if (networkConnected) {
            GetScheduleMethod method = GetScheduleMethod.getMethod();
            result = method.checkSchedule();
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
