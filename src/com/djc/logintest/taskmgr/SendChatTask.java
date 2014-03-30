package com.djc.logintest.taskmgr;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.djc.logintest.activities.MyApplication;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.customexception.DuplicateLoginException;
import com.djc.logintest.customexception.InvalidTokenException;
import com.djc.logintest.dbmgr.info.ChatInfo;
import com.djc.logintest.net.ChatMethod;
import com.djc.logintest.utils.Utils;

public class SendChatTask extends AsyncTask<Void, Void, Integer> {
	private Handler handler;
	private String content;
	private int lastid;
	private List<ChatInfo> list = new ArrayList<ChatInfo>();

	public SendChatTask(Handler handler, String content, int lastid) {
		this.handler = handler;
		this.content = content;
		this.lastid = lastid;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		int result = EventType.NET_WORK_INVALID;
		boolean networkConnected = Utils.isNetworkConnected(MyApplication
				.getInstance());
		if (networkConnected) {
			try {
				list = ChatMethod.getMethod().sendChat(content, lastid);
				result = EventType.SUCCESS;
			} catch (InvalidTokenException e) {
				result = EventType.TOKEN_INVALID;
			} catch (DuplicateLoginException e) {
				result = EventType.PHONE_NUM_IS_ALREADY_LOGIN;
			} catch (Exception e) {
				result = EventType.FAIL;
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
		handler.sendMessage(msg);
	}

}
