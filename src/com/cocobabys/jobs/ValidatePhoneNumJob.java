package com.cocobabys.jobs;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.ValidatePhoneMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;

import android.os.Handler;
import android.os.Message;

public class ValidatePhoneNumJob extends MyJob {

	private Handler hander;
	private String phonenum;

	public ValidatePhoneNumJob(Handler handler, String phonenum) {
		this.hander = handler;
		this.phonenum = phonenum;
	}

	@Override
	public void run() {
		super.run();
		int bret = EventType.PHONE_NUM_IS_INVALID;

		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public Object handle() throws Exception {
				int result = ValidatePhoneMethod.getMethod().validatePhone(
						phonenum);
				return result;
			}
		});
		try {
			bret = (Integer) bind.handle();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Message msg = Message.obtain();
			msg.what = bret;
			hander.sendMessage(msg);
		}
	}
}
