package com.cocobabys.jobs;

import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.MethodResult;
import com.cocobabys.net.NewChatMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;

public class GetChatJob extends MyJob {
	// 最少等2s
	private static final int LIMIT_TIME = 2000;

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
			bret = getBindResult(bind);
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

	// 临时处理一下，本来应该都返回MethodResult，但是有大量老接口，还是使用int做返回，这里做一下适配
	private MethodResult getBindResult(MyProxyImpl bind) throws Exception {
		MethodResult bret = null;
		Object obj = null;
		try {
			obj = bind.handle();
			bret = (MethodResult) obj;
		} catch (ClassCastException e) {
			bret = new MethodResult();
			bret.setResultType((Integer) obj);
		}
		return bret;
	}
}
