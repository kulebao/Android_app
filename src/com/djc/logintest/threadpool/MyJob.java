package com.djc.logintest.threadpool;

import java.util.concurrent.Future;

public class MyJob implements Runnable {

	@Override
	public void run() {

	}

	public Future<?> execute() {
		return MyThreadPoolMgr.getGenericService().submit(this);
	}
}
