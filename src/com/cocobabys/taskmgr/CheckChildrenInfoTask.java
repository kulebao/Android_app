package com.cocobabys.taskmgr;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ChildInfo;
import com.cocobabys.net.ChildMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;

public class CheckChildrenInfoTask extends AsyncTask<Void, Void, Integer> {

	private Handler handler;

	public CheckChildrenInfoTask(Handler handler) {
		this.handler = handler;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public Object handle() throws Exception {
				// int result = ChildMethod.getMethod()
				// .getChildrenInfo();
				ChildInfo selectedChild = DataMgr.getInstance().getSelectedChild();

				int result = ChildMethod.getMethod().getRelationship();
				//再次调用getRelationship,因为在selectedChild为空的情况下，只能获取到当前登录家长的小孩情况
				//selectedChild不为空，则可以通过小孩id，获取到他的全部家长信息，供家园互动使用，这里需要查询2次
				if (selectedChild == null) {
					ChildMethod.getMethod().getRelationship();
				}
				return result;
			}
		});
		Integer result = EventType.NET_WORK_INVALID;
		try {
			result = (Integer) bind.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		sendCheckChildInfoMsg(result);
		sendCheckNewDataMsg();
	}

	private void sendCheckNewDataMsg() {
		// 此时更新后的小孩数据已经写入数据库，在这里通知主界面检查全部数据是否有更新
		// 并提示用户
		Message msg = Message.obtain();
		msg.what = EventType.CHECK_NEW_DATA;
		handler.sendMessage(msg);
	}

	private void sendCheckChildInfoMsg(Integer result) {
		Message msg = Message.obtain();
		msg.what = result;
		handler.sendMessage(msg);
	}
}
