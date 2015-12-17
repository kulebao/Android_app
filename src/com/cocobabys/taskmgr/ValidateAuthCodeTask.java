package com.cocobabys.taskmgr;

import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.EventType;
import com.cocobabys.net.AuthCodeMethod;
import com.cocobabys.utils.Utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class ValidateAuthCodeTask extends AsyncTask<Void, Void, Integer> {

	private Handler hander;
	private String phonenum;
	private String authcode;

	public ValidateAuthCodeTask(Handler handler, String phonenum,
			String authcode) {
		this.hander = handler;
		this.phonenum = phonenum;
		this.authcode = authcode;
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
				result = authCodeMethod.validateAuthCode(phonenum, authcode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		// Account fakeAccount = createFakeAccount();
		Message msg = Message.obtain();
		msg.what = result;
		// msg.obj = fakeAccount;
		hander.sendMessage(msg);

	}

}
