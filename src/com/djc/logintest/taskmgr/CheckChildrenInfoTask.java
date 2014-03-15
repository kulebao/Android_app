package com.djc.logintest.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.net.GetChildrenInfoMethod;
import com.djc.logintest.utils.Utils;

public class CheckChildrenInfoTask extends AsyncTask<Void, Void, Integer> {

    private Handler handler;

    public CheckChildrenInfoTask(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        int result = EventType.NET_WORK_INVALID;
        try {
			boolean networkConnected = Utils.isNetworkConnected(MyApplication.getInstance());
			if (networkConnected) {
			    GetChildrenInfoMethod method = GetChildrenInfoMethod.getMethod();
			    result = method.updateChildrenInfo();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        return result;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        sendCheckChildInfoMsg(result);
        sendCheckNewDataMsg();
    }

	private void sendCheckNewDataMsg() {
		//此时更新后的小孩数据已经写入数据库，在这里通知主界面检查全部数据是否有更新
		//并提示用户
		Message msg = Message.obtain();
        msg.what = EventType.CHECK_NEW_DATA;
        handler.sendMessage(msg);
	}

	private void sendCheckChildInfoMsg(Integer result) {
		Message msg = Message.obtain();
        msg.what = result;
        handler.sendMessage(msg);
	}
}
