package com.cocobabys.taskmgr;

import java.util.List;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.customexception.DuplicateLoginException;
import com.cocobabys.customexception.InvalidTokenException;
import com.cocobabys.dbmgr.info.News;
import com.cocobabys.net.NewsMethod;
import com.cocobabys.utils.Utils;

public class GetNormalNewsTask extends AsyncTask<Void, Void, Integer> {
	// 最少等2s
	private static final int LIMIT_TIME = 1000;
	private Handler handler;
	private int most;
	private long from;
	private long to;
	private List<News> list;
	private int addType = ConstantValue.Type_INSERT_HEAD;

	public GetNormalNewsTask(Handler handler, int most, long from, long to,
			int addType) {
		this.handler = handler;
		this.most = most;
		this.from = from;
		this.to = to;
		this.addType = addType;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		long current = System.currentTimeMillis();
		int result = EventType.NET_WORK_INVALID;
		boolean networkConnected = Utils.isNetworkConnected(MyApplication
				.getInstance());
		if (networkConnected) {
			NewsMethod method = NewsMethod.getMethod();
			try {
				list = method.getNormalNews(most, from, to);
				result = EventType.GET_NOTICE_SUCCESS;
			} catch (InvalidTokenException e) {
				result = EventType.TOKEN_INVALID;
			} catch (DuplicateLoginException e) {
				result = EventType.PHONE_NUM_IS_ALREADY_LOGIN;
			} catch (Exception e) {
				result = EventType.GET_NOTICE_FAILED;
				e.printStackTrace();
			}
		}

		long now = System.currentTimeMillis();
		long ellapse = now - current;
		if (ellapse < LIMIT_TIME) {
			try {
				Thread.sleep(LIMIT_TIME - ellapse);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		Message msg = Message.obtain();
		msg.what = result;
		msg.obj = list;
		msg.arg1 = addType;
		handler.sendMessage(msg);
	}

}
