package com.cocobabys.threadpool;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MyThreadPoolMgr {
	private static final int MAX_THREAD_IN_POOL = 30;
	private static ScheduledExecutorService genericThreadService = Executors
			.newScheduledThreadPool(MAX_THREAD_IN_POOL);

	public synchronized static ScheduledExecutorService getGenericService() {
		if (genericThreadService == null) {
			genericThreadService = Executors
					.newScheduledThreadPool(MAX_THREAD_IN_POOL);
		}

		return genericThreadService;
	}

	public synchronized static void shutdown() {
		if (genericThreadService != null) {
			genericThreadService.shutdown();
			genericThreadService = null;
		}
	}

}
