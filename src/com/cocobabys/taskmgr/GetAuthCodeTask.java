package com.cocobabys.taskmgr;

import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.EventType;
import com.cocobabys.net.AuthCodeMethod;
import com.cocobabys.utils.Utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

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
		int result = EventType.GET_AUTH_CODE_FAIL;
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
