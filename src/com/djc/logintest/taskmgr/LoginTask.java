package com.djc.logintest.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.net.LoginMethod;

public class LoginTask extends AsyncTask<Void, Void, Integer> {

    private Handler hander;
    private String pwd;
    private String account;

    public LoginTask(Handler handler, String account, String pwd) {
        this.hander = handler;
        this.account = account;
        this.pwd = pwd;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        LoginMethod loginMethod = LoginMethod.getLoginMethod();
        return loginMethod.login(pwd, account);
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Message msg = Message.obtain();
        msg.what = result;
        if (result == EventType.LOGIN_SUCCESS) {
            msg.arg2 = ConstantValue.DO_NOT_CANCEL_DIALOG;
        }
        hander.sendMessage(msg);
    }
}
