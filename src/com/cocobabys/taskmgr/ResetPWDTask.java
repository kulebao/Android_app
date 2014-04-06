package com.cocobabys.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.ResetPWDMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;

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
		Log.w("djc", "ResetPWDTask! doInBackground");
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
			Log.w("djc", "ResetPWDTask!");
			result = (Integer) bind.handle();
			Log.w("djc", "ResetPWDTask! result="+result);
		} catch (Exception e) {
			Log.w("djc", "Exception e="+e.toString());
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
