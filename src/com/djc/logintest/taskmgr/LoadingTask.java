package com.djc.logintest.taskmgr;

import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.constant.EventType;
import com.djc.logintest.utils.Utils;

public class LoadingTask extends AsyncTask<Void, Void, Void> {

	private static final long LIMIT_TIME = 2000;
	private Handler hander;
	private int resultEvent;

	public LoadingTask(Handler handler) {
		this.hander = handler;
	}

	@Override
	protected Void doInBackground(Void... params) {
		try {
			long current = System.currentTimeMillis();
			if (Utils.isFirstStart()) {
				resultEvent = EventType.LOADING_TO_GUARD;
			} else if (Utils.isLoginout()) {
				resultEvent = EventType.LOADING_TO_VALIDATEPHONE;
			} else {
				resultEvent = EventType.LOADING_TO_MAIN;
			}
			long now = System.currentTimeMillis();
			long ellapse = now - current;
			if (ellapse < LIMIT_TIME) {
				TimeUnit.MILLISECONDS.sleep(LIMIT_TIME - ellapse);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		Message msg = Message.obtain();
		msg.what = resultEvent;
		hander.sendMessage(msg);
	}

}
