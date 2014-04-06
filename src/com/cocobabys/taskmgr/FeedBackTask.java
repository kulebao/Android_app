package com.cocobabys.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.FeedBackMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;

public class FeedBackTask extends AsyncTask<Void, Void, Integer> {
	private Handler handler;
	private String content;

	public FeedBackTask(Handler handler, String content) {
		this.handler = handler;
		this.content = content;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public Object handle() throws Exception {
				int result = FeedBackMethod.getMethod().feedBack(content);
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
