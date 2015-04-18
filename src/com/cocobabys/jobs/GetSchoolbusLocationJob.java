package com.cocobabys.jobs;

import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.net.MethodResult;
import com.cocobabys.net.SchoolbusMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

//获取定位器最后一次坐标任务
public class GetSchoolbusLocationJob extends MyJob {
	private Handler handler;

	public GetSchoolbusLocationJob(Handler handler) {
		this.handler = handler;
	}

	@Override
	public void run() {
		getLoaction();
	}

	private void getLoaction() {
		MethodResult bret = new MethodResult(EventType.GET_LAST_BUS_LOCATION_FAIL);
		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public MethodResult handle() throws Exception {
				MethodResult result = SchoolbusMethod.getMethod().getLocation(
						DataMgr.getInstance().getSelectedChild().getServer_id());
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
