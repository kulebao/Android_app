package com.cocobabys.jobs;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.InvitationMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

import android.os.Handler;
import android.os.Message;

public class InvitationJob extends MyJob {
	private Handler handler;
	private String phone;
	private String name;
	private String relation;
	private String vCode;

	public InvitationJob(Handler handler, String phone, String name, String relation, String vCode) {
		this.handler = handler;
		this.phone = phone;
		this.relation = relation;
		this.name = name;
		this.vCode = vCode;
	}

	@Override
	public void run() {
		MethodResult bret = new MethodResult(EventType.INVITE_FAIL);
		try {
			MyProxy proxy = new MyProxy();
			MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
				@Override
				public MethodResult handle() throws Exception {
					MethodResult result = InvitationMethod.getMethod().invite(phone, name, relation, vCode);
					return result;
				}
			});

			bret = MethodUtils.getBindResult(bind);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Message msg = Message.obtain();
			msg.what = bret.getResultType();
			handler.sendMessage(msg);
		}

	}

}
