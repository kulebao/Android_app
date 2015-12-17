package com.cocobabys.taskmgr;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.CookbookMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class GetCookbookTask extends AsyncTask<Void, Void, Integer> {

	private Handler handler;

	public GetCookbookTask(Handler handler) {
		this.handler = handler;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public Object handle() throws Exception {
				int result = CookbookMethod.getMethod().getCookBook();
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
		handler.sendMessage(msg);
	}
}
