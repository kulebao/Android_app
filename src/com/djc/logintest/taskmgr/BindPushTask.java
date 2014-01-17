package com.djc.logintest.taskmgr;

import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.net.HttpsMethod;
import com.djc.logintest.push.PushModel;
import com.djc.logintest.utils.Utils;

public class BindPushTask extends AsyncTask<Void, Void, Integer> {
    // 最多等待30秒，bind过程
    private static final int MAX_WAIT_FOR_BIND = 60;
    private Handler hander;
    private String phonenum;

    public BindPushTask(Handler handler, String phonenum) {
        this.hander = handler;
        this.phonenum = phonenum;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int result = EventType.BIND_FAILED;

        try {
            // 检查是否之前已经绑定成功过了，如果绑定成功了，就只需要向服务器发送绑定信息
            if (checkBindInfo()) {
                return sendInfoToSelfServer();
            }

            result = doBindToSelfServer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public int doBindToSelfServer() throws InterruptedException {
        int result = EventType.BIND_FAILED;
        // 发起绑定
        PushModel.getPushModel().bind();
        // 如果绑定成功，PushEventHandler会记录下绑定信息
        for (int i = 0; i < MAX_WAIT_FOR_BIND; i++) {
            if (checkBindInfo()) {
                // 发送数据给服务器
                result = sendInfoToSelfServer();
                break;
            }

            TimeUnit.SECONDS.sleep(1);
        }
        return result;
    }

    public int sendInfoToSelfServer() {
        return HttpsMethod.sendBinfInfo(phonenum, Utils.getProp(JSONConstant.USER_ID),
                Utils.getProp(JSONConstant.CHANNEL_ID));
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        Message msg = Message.obtain();
        msg.what = result;
        hander.sendMessage(msg);
    }

    private boolean checkBindInfo() {
        return !"".equals(Utils.getProp(JSONConstant.USER_ID));
    }
}
