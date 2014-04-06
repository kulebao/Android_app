package com.cocobabys.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.SwipeCardMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;

public class UpdateCalendarTask extends AsyncTask<Void, Void, Integer> {

	private Handler handler;
	private long from;
	private long to;

	public UpdateCalendarTask(Handler handler, long from, long to) {
		this.handler = handler;
		this.from = from;
		this.to = to;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public Object handle() throws Exception {
				int result = SwipeCardMethod.getMethod().getSwipecardRecord(
						from, to);
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
