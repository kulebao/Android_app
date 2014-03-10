package com.djc.logintest.taskmgr;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.dbmgr.info.ChatInfo;
import com.djc.logintest.net.ChatMethod;
import com.djc.logintest.utils.Utils;

public class GetChatTask extends AsyncTask<Void, Void, Integer> {
	// 最少等2s
	private static final int LIMIT_TIME = 2000;

	private Handler handler;
	private int most;
	private long from;
	private long to;
	private List<ChatInfo> list = new ArrayList<ChatInfo>();

	private String sort;

	private int addType;

	public GetChatTask(Handler handler, int most, long from, long to,
			int addType, String sort) {
		this.handler = handler;
		this.most = most;
		this.from = from;
		this.to = to;
		this.addType = addType;
		this.sort = sort;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		long current = System.currentTimeMillis();
		int result = EventType.NET_WORK_INVALID;

		boolean networkConnected = Utils.isNetworkConnected(MyApplication
				.getInstance());
		if (networkConnected) {
			ChatMethod method = ChatMethod.getMethod();
			try {
				list = method.getChatInfo(most, from, to, sort);
				result = EventType.SUCCESS;
			} catch (Exception e) {
				result = EventType.FAIL;
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