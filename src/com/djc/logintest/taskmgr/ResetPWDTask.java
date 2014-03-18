package com.djc.logintest.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.net.ResetPWDMethod;
import com.djc.logintest.proxy.MyProxy;
import com.djc.logintest.proxy.MyProxyImpl;

public class ResetPWDTask extends AsyncTask<Void, Void, Integer> {

	private Handler hander;
	private String phonenum;
	private String newPwd;
	private String authcode;

	public ResetPWDTask(Handler handler, String phonenum, String authcode,
			String newPwd) {
		this.hander = handler;
		this.phonenum = phonenum;
		this.authcode = authcode;
		this.newPwd = newPwd;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {

			@Override
			public Object handle() throws Exception {
				int result = ResetPWDMethod.getResetPwdMethod().resetPwd(
						authcode, newPwd, phonenum);
				return result;
			}
		});
		Integer result = EventType.NET_WORK_INVALID;
		try {
			result = (Integer) bind.handle();
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
