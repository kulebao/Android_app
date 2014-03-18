package com.djc.logintest.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.net.FeedBackMethod;
import com.djc.logintest.net.SchoolMethod;
import com.djc.logintest.proxy.MyProxy;
import com.djc.logintest.proxy.MyProxyImpl;
import com.djc.logintest.utils.Utils;

public class GetSchoolInfoTask extends AsyncTask<Void, Void, Integer> {

	private Handler handler;

	public GetSchoolInfoTask(Handler handler) {
		this.handler = handler;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public Object handle() throws Exception {
				int result = SchoolMethod.getGetAuthCodeMethod()
						.checkSchoolInfo();
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
