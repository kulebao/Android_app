package com.cocobabys.jobs;

import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.ExpMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;

public class DeleteExpJob extends MyJob {
	private Handler handler;
	private long expid;
	private String childid;

	public DeleteExpJob(Handler handler, long expid, String childid) {
		this.handler = handler;
		this.expid = expid;
		this.childid = childid;
	}

	@Override
	public void run() {
		MethodResult bret = new MethodResult(EventType.DELETE_EXP_FAIL);

		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public MethodResult handle() throws Exception {
				MethodResult result = ExpMethod.getMethod().deleteExp(expid,
						childid);
				return result;
			}
		});

		try {
			bret = (MethodResult) bind.handle();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Message msg = Message.obtain();
			msg.what = bret.getResultType();
			handler.sendMessage(msg);
		}

	}

}
