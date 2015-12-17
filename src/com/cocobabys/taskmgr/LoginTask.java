package com.cocobabys.taskmgr;

import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.net.LoginMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class LoginTask extends AsyncTask<Void, Void, Integer> {

	private Handler hander;
	private String pwd;
	private String account;

	public LoginTask(Handler handler, String account, String pwd) {
		this.hander = handler;
		this.account = account;
		this.pwd = pwd;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public Object handle() throws Exception {
				int result = LoginMethod.getLoginMethod().login(pwd, account);
				return result;
			}
		});
		Integer bret = EventType.NET_WORK_INVALID;
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
		if (result == EventType.LOGIN_SUCCESS) {
			msg.arg2 = ConstantValue.DO_NOT_CANCEL_DIALOG;
		}
		hander.sendMessage(msg);
	}
}
