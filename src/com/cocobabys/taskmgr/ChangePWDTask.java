package com.cocobabys.taskmgr;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.ResetPWDMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class ChangePWDTask extends AsyncTask<Void, Void, Integer> {

	private Handler hander;
	private String phonenum;
	private String newPwd;
	private String oldPwd;

	public ChangePWDTask(Handler handler, String phonenum, String oldPwd,
			String newPwd) {
		this.hander = handler;
		this.phonenum = phonenum;
		this.oldPwd = oldPwd;
		this.newPwd = newPwd;
	}

	@Override
	protected Integer doInBackground(Void... params) {

		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {

			@Override
			public Object handle() throws Exception {
				int result = ResetPWDMethod.getResetPwdMethod().changePwd(
						oldPwd, newPwd, phonenum);
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
