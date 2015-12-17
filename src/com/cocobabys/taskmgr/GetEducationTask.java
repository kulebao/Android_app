package com.cocobabys.taskmgr;

import java.util.List;

import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.customexception.DuplicateLoginException;
import com.cocobabys.customexception.InvalidTokenException;
import com.cocobabys.dbmgr.info.EducationInfo;
import com.cocobabys.net.EducationMethod;
import com.cocobabys.utils.Utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class GetEducationTask extends AsyncTask<Void, Void, Integer> {
	private Handler handler;
	private int most;
	private long from;
	private long to;
	private List<EducationInfo> list;
	private int addType = ConstantValue.TYPE_GET_HEAD;

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
