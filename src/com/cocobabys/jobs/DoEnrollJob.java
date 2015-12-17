package com.cocobabys.jobs;

import com.cocobabys.bean.ActionInfo;
import com.cocobabys.constant.EventType;
import com.cocobabys.net.ActionMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

import android.os.Handler;
import android.os.Message;

public class DoEnrollJob extends MyJob {
	private static final long LIMIT_TIME = 500;
	private Handler handler;
	private ActionInfo actioninfo;

	public DoEnrollJob(Handler handler, ActionInfo actioninfo) {
		this.handler = handler;
		this.actioninfo = actioninfo;
	}

	@Override
	public void run() {
		long current = System.currentTimeMillis();
		MethodResult bret = new MethodResult(EventType.ACTION_DO_ENROLL_FAIL);
		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {

			@Override
			public MethodResult handle() throws Exception {
				MethodResult result = ActionMethod.getMethod().doEnroll(
						actioninfo);
				return result;
			}
		});

		try {
			long now = System.currentTimeMillis();
			bret = MethodUtils.getBindResult(bind);
			long ellapse = now - current;
			if (ellapse < LIMIT_TIME) {
				try {
					Thread.sleep(LIMIT_TIME - ellapse);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

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
