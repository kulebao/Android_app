package com.cocobabys.jobs;

import com.cocobabys.net.AdMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;

public class GetADJob extends MyJob {

	public GetADJob() {
	}

	@Override
	public void run() {

		MyProxy proxy = new MyProxy();
		proxy.bind(new MyProxyImpl() {
			@Override
			public MethodResult handle() throws Exception {
				MethodResult result = AdMethod.getMethod().getInfo();
				return result;
			}
		});
	}

}
