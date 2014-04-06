package com.cocobabys.taskmgr;

import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.net.PushMethod;
import com.cocobabys.push.PushModel;
import com.cocobabys.utils.Utils;

public class BindPushTask extends AsyncTask<Void, Void, Integer> {
	// 最多等待60秒，bind过程
	private static final int MAX_WAIT_FOR_BIND = 60;
	private Handler hander;
	private String phonenum;

	public BindPushTask(Handler handler, String phonenum) {
		this.hander = handler;
		this.phonenum = phonenum;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		if (!Utils.isNetworkConnected(MyApplication.getInstance())) {
			return EventType.NET_WORK_INVALID;
		}

		int result = EventType.BIND_FAILED;
		try {
			// 检查是否之前已经绑定成功过了，如果绑定成功了，就只需要向服务器发送绑定信息
			if (checkBindInfo()) {
				Log.w("DJC", "BindPushTask aleady bind send it to server!");
				return sendInfoToSelfServer();
			}

			result = doBindToSelfServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public int doBindToSelfServer() throws Exception {
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

	public int sendInfoToSelfServer() throws Exception {
		PushMethod method = PushMethod.getMethod();
		return method.sendBinfInfo(phonenum,
				Utils.getUndeleteableProp(JSONConstant.USER_ID),
				Utils.getUndeleteableProp(JSONConstant.CHANNEL_ID));
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		Message msg = Message.obtain();
		msg.what = result;
		hander.sendMessage(msg);
	}

	private boolean checkBindInfo() {
		return !"".equals(Utils.getUndeleteableProp(JSONConstant.USER_ID));
	}
}
