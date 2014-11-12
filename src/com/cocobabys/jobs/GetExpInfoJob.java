package com.cocobabys.jobs;

import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.ExpMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

public class GetExpInfoJob extends MyJob {
	private Handler handler;
	private int year;
	private String month;

	public GetExpInfoJob(Handler handler, int year, String month) {
		this.handler = handler;
		this.year = year;
		this.month = month;
	}

	@Override
	public void run() {
		MethodResult bret = new MethodResult(EventType.GET_EXP_INFO_FAIL);

		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public MethodResult handle() throws Exception {
				MethodResult result = ExpMethod.getMethod().getExpInfoByYearAndMonth(year, month);
				return result;
			}
		});

		try {
			bret = MethodUtils.getBindResult(bind);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Message msg = Message.obtain();
			msg.what = bret.getResultType();
			msg.obj = bret.getResultObj();
			handler.sendMessage(msg);
		}
	}
}
