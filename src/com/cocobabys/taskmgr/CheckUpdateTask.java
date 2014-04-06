package com.cocobabys.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.ChechUpdateMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;

public class CheckUpdateTask extends AsyncTask<Void, Void, Integer> {

	private Handler hander;
	private int versionCode;

	public CheckUpdateTask(Handler handler, String account, int versionCode) {
		this.hander = handler;
		this.versionCode = versionCode;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {

			@Override
			public Object handle() throws Exception {
				int result = ChechUpdateMethod.getChechUpdateMethod()
						.chechUpdate(versionCode);
				return result;
			}
		});
		Integer result = EventType.HAS_NO_VERSION;
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
