package com.cocobabys.jobs;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.MethodResult;
import com.cocobabys.net.TeacherMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

import android.os.Handler;
import android.os.Message;

public class GetTeacherListJob extends MyJob {
	private Handler handler;
	private String classid;

	public GetTeacherListJob(Handler handler, String classid) {
		this.handler = handler;
		this.classid = classid;
	}

	@Override
	public void run() {
		MethodResult bret = new MethodResult(EventType.GET_TEACHER_FAIL);
		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public MethodResult handle() throws Exception {
				MethodResult result = TeacherMethod.getMethod().getTeacherListByClassID(classid);
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
