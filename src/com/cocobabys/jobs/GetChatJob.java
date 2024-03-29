package com.cocobabys.jobs;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.MethodResult;
import com.cocobabys.net.NewChatMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

import android.os.Handler;
import android.os.Message;

public class GetChatJob extends MyJob {
	// 最少等2s
	private static final int LIMIT_TIME = 500;

	private Handler handler;
	private int most;
	private long from;
	private long to;
	private int addType;
	private String childid;

	public GetChatJob(Handler handler, int most, long from, long to,
			int addType, String childid) {
		this.handler = handler;
		this.most = most;
		this.from = from;
		this.to = to;
		this.addType = addType;
		this.childid = childid;
	}

	@Override
	public void run() {
		long current = System.currentTimeMillis();

		MethodResult bret = new MethodResult(EventType.GET_CHAT_FAIL);

		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public MethodResult handle() throws Exception {
				MethodResult result = NewChatMethod.getMethod().getChatInfo(
						most, from, to, childid);
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
			msg.arg1 = addType;
			handler.sendMessage(msg);
		}

	}
}
