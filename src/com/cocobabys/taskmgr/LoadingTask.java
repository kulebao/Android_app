package com.cocobabys.taskmgr;

import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.EventType;
import com.cocobabys.net.PushMethod;
import com.cocobabys.net.SchoolMethod;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

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

			if (DataUtils.isFirstStart()) {
				resultEvent = EventType.LOADING_TO_GUARD;
			} else if (DataUtils.isLoginout()) {
				resultEvent = EventType.LOADING_TO_VALIDATEPHONE;
			} else {
				checkDbUpdate();

				resultEvent = EventType.LOADING_TO_MAIN;
				// 如果是已经登录过的，每次启动都bind一次，刷新cookie
				if (Utils.isNetworkConnected(MyApplication.getInstance())) {
					PushMethod.getMethod().sendBinfInfo();
				}
				
				SchoolMethod.getGetAuthCodeMethod().saveSchoolConfig();
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

	// 检查是否有数据库正在升级，如果有，需要等到升级完毕再执行下一步操作
	private void checkDbUpdate() {
		try {
			// 先等300ms，以免sqliteHelper 的 upgrade还没有触发
			Thread.sleep(300);
			while (MyApplication.getInstance().isDbUpdating()) {
				// 每次等300ms
				Thread.sleep(300);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		Message msg = Message.obtain();
		msg.what = resultEvent;
		hander.sendMessage(msg);
	}

}
