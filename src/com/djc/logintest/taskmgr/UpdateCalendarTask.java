package com.djc.logintest.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.net.SwipeCardMethod;
import com.djc.logintest.utils.Utils;

public class UpdateCalendarTask extends AsyncTask<Void, Void, Integer> {

    private Handler handler;
    private long from;
    private long to;

    public UpdateCalendarTask(Handler handler, long from, long to) {
        this.handler = handler;
        this.from = from;
        this.to = to;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int result = EventType.NET_WORK_INVALID;
        try {
            boolean networkConnected = Utils.isNetworkConnected(MyApplication.getInstance());
            if (networkConnected) {
                SwipeCardMethod method = SwipeCardMethod.getMethod();
                result = method.getSwipecardRecord(from, to);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
