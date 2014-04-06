package com.cocobabys.taskmgr;

import android.os.AsyncTask;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.TeacherMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;

public class GetTeacherTask extends AsyncTask<Void, Void, Integer> {

	private String phones;

	public GetTeacherTask(String phones) {
		this.phones = phones;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public Object handle() throws Exception {
				int result = TeacherMethod.getMethod().getTeacherInfo(phones);
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
	}

}
