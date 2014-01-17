package com.djc.logintest.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.net.ChechUpdateMethod;

public class CheckUpdateTask extends AsyncTask<Void, Void, Integer> {

    private Handler hander;
    private String account;
    private int versionCode;

    public CheckUpdateTask(Handler handler, String account, int versionCode) {
        this.hander = handler;
        this.account = account;
        this.versionCode = versionCode;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        ChechUpdateMethod chechUpdateMethod = ChechUpdateMethod.getChechUpdateMethod();
        return chechUpdateMethod.chechUpdate(versionCode);
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Message msg = Message.obtain();
        msg.what = result;
        hander.sendMessage(msg);
    }
}
