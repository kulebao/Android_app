package com.djc.logintest.taskmgr;

import java.io.IOException;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.ServerUrls;
import com.djc.logintest.net.FeedBackMethod;
import com.djc.logintest.net.HttpsMethod;
import com.djc.logintest.net.ValidatePhoneMethod;
import com.djc.logintest.proxy.MyProxy;
import com.djc.logintest.proxy.MyProxyImpl;

public class ValidatePhoneNumTask extends AsyncTask<Void, Void, Integer> {

	private Handler hander;
	private String phonenum;

	public ValidatePhoneNumTask(Handler handler, String phonenum) {
		this.hander = handler;
		this.phonenum = phonenum;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public Object handle() throws Exception {
				int result = ValidatePhoneMethod.getMethod().validatePhone(
						phonenum);
				return result;
			}
		});
		Integer bret = EventType.PHONE_NUM_IS_INVALID;
		try {
			bret = (Integer) bind.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bret;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		Message msg = Message.obtain();
		msg.what = result;
		hander.sendMessage(msg);
	}

}
