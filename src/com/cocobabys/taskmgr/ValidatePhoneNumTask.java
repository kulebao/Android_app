package com.cocobabys.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.ValidatePhoneMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;

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
