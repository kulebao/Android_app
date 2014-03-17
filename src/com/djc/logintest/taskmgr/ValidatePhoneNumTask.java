package com.djc.logintest.taskmgr;

import java.io.IOException;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.ServerUrls;
import com.djc.logintest.net.HttpsMethod;
import com.djc.logintest.net.ValidatePhoneMethod;

public class ValidatePhoneNumTask extends AsyncTask<Void, Void, Integer> {

	private Handler hander;
	private String phonenum;

	public ValidatePhoneNumTask(Handler handler, String phonenum) {
		this.hander = handler;
		this.phonenum = phonenum;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		int result = EventType.PHONE_NUM_IS_INVALID;
		try {
			// result =
			// HttpsMethod.validatePhone(ServerUrls.CHECK_PHONE_NUM_URL,
			// phonenum);
			result = ValidatePhoneMethod.getMethod().validatePhone(phonenum);
		} catch (Exception e) {
			e.printStackTrace();
			result = EventType.NET_WORK_INVALID;
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
