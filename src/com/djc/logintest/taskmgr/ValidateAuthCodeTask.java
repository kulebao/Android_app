package com.djc.logintest.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.net.HttpsMethod;

public class ValidateAuthCodeTask extends AsyncTask<Void, Void, Integer> {

    private Handler hander;
    private String phonenum;
    private String authcode;

    public ValidateAuthCodeTask(Handler handler, String phonenum, String authcode) {
        this.hander = handler;
        this.phonenum = phonenum;
        this.authcode = authcode;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        return HttpsMethod.validateRegAuthCode(phonenum, authcode);
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        // Account fakeAccount = createFakeAccount();
        Message msg = Message.obtain();
        // 当消息是EventType.AUTH_CODE_IS_VALID时，不要取消dialog，因为后续还要发起push绑定
        // 以及回传绑定成功后id等操作
        if (result == EventType.AUTH_CODE_IS_VALID) {
            msg.arg2 = ConstantValue.DO_NOT_CANCEL_DIALOG;
        }
        msg.what = result;
        // msg.obj = fakeAccount;
        hander.sendMessage(msg);

    }

}
