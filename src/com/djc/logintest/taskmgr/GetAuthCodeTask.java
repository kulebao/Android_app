package com.djc.logintest.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.net.AuthCodeMethod;
import com.djc.logintest.utils.Utils;

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
		int result = EventType.NET_WORK_INVALID;
		try {
			boolean networkConnected = Utils.isNetworkConnected(MyApplication
					.getInstance());
			if (networkConnected) {
				AuthCodeMethod authCodeMethod = AuthCodeMethod
						.getGetAuthCodeMethod();
				result = authCodeMethod.getAuthCode(phonenum, type);
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
		hander.sendMessage(msg);
	}

}
