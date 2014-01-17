package com.djc.logintest.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.net.GetAuthCodeMethod;

public class GetAuthCodeTask extends AsyncTask<Void, Void, Integer> {

    private Handler hander;
    private String phonenum;
    private int type;

    public GetAuthCodeTask(Handler handler, String phonenum, int type) {
        this.hander = handler;
        this.phonenum = phonenum;
        this.type = type;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        return GetAuthCodeMethod.getGetAuthCodeMethod().getAuthCode(phonenum, type);
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Message msg = Message.obtain();
        msg.what = result;
        hander.sendMessage(msg);
    }

}
