package com.djc.logintest.taskmgr;

import java.util.List;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.customexception.DuplicateLoginException;
import com.djc.logintest.customexception.InvalidTokenException;
import com.djc.logintest.dbmgr.info.EducationInfo;
import com.djc.logintest.net.EducationMethod;
import com.djc.logintest.utils.Utils;

public class GetEducationTask extends AsyncTask<Void, Void, Integer> {
	private Handler handler;
	private int most;
	private long from;
	private long to;
	private List<EducationInfo> list;
	private int addType = ConstantValue.Type_INSERT_HEAD;

	public GetEducationTask(Handler handler, int most, long from, long to,
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
			EducationMethod method = EducationMethod.getMethod();
			try {
				list = method.getEdus(most, from, to);
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
