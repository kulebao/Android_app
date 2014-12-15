package com.cocobabys.jobs;

import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cocobabys.constant.EventType;
import com.cocobabys.threadpool.MyJob;

public class RefreshLocationJob extends MyJob {
	private Handler handler;
	// 最大倒计时
	private int maxCountDown;
	// 当前计时
	private int currentCount;

	public RefreshLocationJob(Handler handler, int maxCountDown) {
		this.handler = handler;
		this.maxCountDown = maxCountDown;
		// 第一次任务，立即触发定位
		this.currentCount = maxCountDown;
	}

	@Override
	public void run() {
		Log.d("DDD ",
				"AAA RefreshLocationJob  run= "
						+ RefreshLocationJob.this.isCancel());
		while (true) {
			try {
				Log.d("", "AAA sendCountDownMessage");
				sendCountDownMessage();
				TimeUnit.SECONDS.sleep(1);
				currentCount--;

				if (currentCount < 1) {
					currentCount = maxCountDown;
				}

			} catch (InterruptedException e) {
				Log.d("", "AAA sendCountDownMessage e=" + e.getMessage());
				e.printStackTrace();
				break;
			}
		}

	}

	private void sendCountDownMessage() {
		Log.d("DDD ", "AAA sendCountDownMessage  currentCount=" + currentCount);
		Message message = Message.obtain();
		message.what = EventType.COUNTDOWN_EVENT;
		message.arg1 = currentCount;
		handler.sendMessage(message);
	}
}
