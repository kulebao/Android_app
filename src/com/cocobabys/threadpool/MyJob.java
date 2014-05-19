package com.cocobabys.threadpool;

import java.util.concurrent.Future;

public class MyJob implements Runnable {

	private Future<?> future;

	@Override
	public void run() {

	}

	public Future<?> execute() {
		this.future = MyThreadPoolMgr.getGenericService().submit(this);
		return future;
	}

	public void cancel(boolean mayInterruptIfRunning) {
		if (future != null) {
			future.cancel(mayInterruptIfRunning);
		}
	}

	public boolean isDone() {
		if (future != null) {
			return future.isDone();
		}
		return true;
	}

	public boolean isCancel() {
		if (future != null) {
			return future.isCancelled();
		}
		return true;
	}
}
