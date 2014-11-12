package com.cocobabys.jobs;

import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.MethodResult;
import com.cocobabys.net.NewChatMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

public class SendChatJob extends MyJob {
	private Handler handler;
	private String childid;
	private String content;
	private long lastid;

	public SendChatJob(Handler handler, String content, String childid, long lastid) {
		this.handler = handler;
		this.content = content;
		this.childid = childid;
		this.lastid = lastid;
	}

	@Override
	public void run() {
		MethodResult bret = new MethodResult(EventType.SEND_CHAT_FAIL);

		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public MethodResult handle() throws Exception {
				MethodResult result = NewChatMethod.getMethod().sendChat(content, lastid, childid);
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
