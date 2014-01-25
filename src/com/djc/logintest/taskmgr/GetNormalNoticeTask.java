package com.djc.logintest.taskmgr;

import java.util.List;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.dbmgr.info.Notice;
import com.djc.logintest.net.GetNormalNoticeMethod;
import com.djc.logintest.utils.Utils;

public class GetNormalNoticeTask extends AsyncTask<Void, Void, Integer> {
	public static final int Type_INSERT_HEAD = 0;
	public static final int Type_INSERT_TAIl = 1;

	private Handler handler;
	private int most;
	private long from;
	private long to;
	private List<Notice> list;
	private int addType = Type_INSERT_HEAD;

	public GetNormalNoticeTask(Handler handler, int most, long from, long to,
			int addType) {
		this.handler = handler;
		this.most = most;
		this.from = from;
		this.to = to;
		this.addType = addType;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		int result = EventType.NET_WORK_INVALID;
		boolean networkConnected = Utils.isNetworkConnected(MyApplication
				.getInstance());
		if (networkConnected) {
			GetNormalNoticeMethod method = GetNormalNoticeMethod.getMethod();
			try {
				list = method.getNormalNotice(most, from, to);
				result = EventType.GET_NOTICE_SUCCESS;
			} catch (Exception e) {
				result = EventType.GET_NOTICE_FAILED;
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
