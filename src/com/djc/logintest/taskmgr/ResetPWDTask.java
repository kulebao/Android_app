package com.djc.logintest.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.net.ResetPWDMethod;

public class ResetPWDTask extends AsyncTask<Void, Void, Integer> {

    private Handler hander;
    private String phonenum;
    private String newPwd;
    private String authcode;

    public ResetPWDTask(Handler handler, String phonenum, String authcode, String newPwd) {
        this.hander = handler;
        this.phonenum = phonenum;
        this.authcode = authcode;
        this.newPwd = newPwd;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        ResetPWDMethod method = ResetPWDMethod.getResetPwdMethod();
        return method.resetPwd(authcode, newPwd, phonenum);
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Message msg = Message.obtain();
        msg.what = result;
        hander.sendMessage(msg);
    }

}
