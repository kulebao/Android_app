package com.cocobabys.jobs;

import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.MethodResult;
import com.cocobabys.net.NewChatMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;

public class DeleteChatJob extends MyJob {
	private Handler handler;
	private long chatid;
	private String childid;

	public DeleteChatJob(Handler handler, long chatid, String childid) {
		this.handler = handler;
		this.chatid = chatid;
		this.childid = childid;
	}

	@Override
	public void run() {
		MethodResult bret = new MethodResult(EventType.DELETE_CHAT_FAIL);

		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public MethodResult handle() throws Exception {
				MethodResult result = NewChatMethod.getMethod().deleteChat(
						chatid, childid);
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
