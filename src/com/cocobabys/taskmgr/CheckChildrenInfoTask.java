package com.cocobabys.taskmgr;

import java.util.List;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import io.rong.imlib.model.Conversation.ConversationType;

import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ChildInfo;
import com.cocobabys.dbmgr.info.IMGroupInfo;
import com.cocobabys.im.IMHelper;
import com.cocobabys.net.ChildMethod;
import com.cocobabys.net.IMMethod;
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
				int result = ChildMethod.getMethod().getRelationship();
				return result;
			}
		});

		Integer result = EventType.NET_WORK_INVALID;
		try {
			result = (Integer) bind.handle();

			updateIMGroupInfo();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// 如果班级比群组少，那么退出班级相关群组
	// private void updateIMGroupInfo(){
	// List<String> allClassID = DataMgr.getInstance().getAllClassID();
	// List<IMGroupInfo> allIMGroupInfo =
	// DataMgr.getInstance().getAllIMGroupInfo();
	//
	// for(final IMGroupInfo groupInfo : allIMGroupInfo){
	// if(!allClassID.contains(groupInfo.getClass_id() + "")){
	// // IMHelper.quitGroup(groupInfo);
	// boolean ret = IMMethod.getMethod().quitGroupInfo(groupInfo.getClass_id()
	// + "");
	// Log.d("", "quit group info=" + groupInfo.toString() + " ret=" + ret);
	// if(ret){
	// DataMgr.getInstance().deleteGroup(groupInfo.getGroup_id());
	// }
	// }
	// }
	// }

	// 如果班级比群组少，那么退出班级相关群组
	private void updateIMGroupInfo() {
		List<ChildInfo> allChildrenInfo = DataMgr.getInstance().getAllChildrenInfo();
		List<IMGroupInfo> allIMGroupInfo = DataMgr.getInstance().getAllIMGroupInfo();

		outer: for (IMGroupInfo groupInfo : allIMGroupInfo) {
			for (ChildInfo childInfo : allChildrenInfo) {
				if (childInfo.getClass_id().equals(groupInfo.getClass_id() + "")) {
					updateGroup(groupInfo, childInfo);
					continue outer;
				}
			}
			quitGroup(groupInfo);
		}
	}

	private boolean quitGroup(IMGroupInfo groupInfo) {
		boolean ret = IMMethod.getMethod().quitGroupInfo(groupInfo.getClass_id() + "");
		Log.d("", "quit group info=" + groupInfo.toString() + " ret=" + ret);
		if (ret) {
			DataMgr.getInstance().deleteGroup(groupInfo.getGroup_id());
			IMHelper.removeConversation(groupInfo.getGroup_id(), ConversationType.GROUP);
		}
		return ret;
	}

	private void updateGroup(IMGroupInfo groupInfo, ChildInfo childInfo) {
		// 班级名称可能会有更新，这里做一次处理
		groupInfo.setGroup_name(childInfo.getClass_name());
		DataMgr.getInstance().addIMGroupInfo(groupInfo);
		IMHelper.updateGroupInfoCache(groupInfo);
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
