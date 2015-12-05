package com.cocobabys.jobs;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.OperationCallback;
import android.os.Handler;
import android.util.Log;

import java.util.List;

import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.info.IMGroupInfo;
import com.cocobabys.net.IMMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

public class JoinGroupJobOld extends MyJob {

	private Handler handler;
	private List<String> classidList;

	public JoinGroupJobOld(Handler handler, List<String> classidList) {
		this.handler = handler;
		this.classidList = classidList;
	}

	@Override
	public void run() {
		try {
			MyProxy proxy = new MyProxy();
			MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
				@Override
				public MethodResult handle() throws Exception {
					boolean bsuccess = false;

					for (String classid : classidList) {
						MethodResult result = IMMethod.getMethod().getGroupInfo(classid);
						if (result.getResultType() == EventType.GET_IM_GROUP_SUCCESS) {
							bsuccess = true;
							joinGroup((IMGroupInfo) result.getResultObj());
						}
					}

					// 一次都没有成功，没有执行到加入群组的操作，这里需要发送失败通知
					if (!bsuccess) {
						handler.sendEmptyMessage(EventType.JOIN_IM_GROUP_FAIL);
					}

					return null;
				}
			});

			MethodUtils.getBindResult(bind);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void joinGroup(IMGroupInfo groupInfo) {

		RongIM.getInstance().getRongIMClient().joinGroup(groupInfo.getGroup_id(), groupInfo.getGroup_name(),
				new OperationCallback() {

					@Override
					public void onSuccess() {
						Log.d("", "DDD JOIN_IM_GROUP_SUCCESS");
						handler.sendEmptyMessage(EventType.JOIN_IM_GROUP_SUCCESS);
					}

					@Override
					public void onError(ErrorCode errorCode) {
						Log.d("", "DDD JOIN_IM_GROUP_FAIL err :" + errorCode.getMessage() + " code="
								+ errorCode.getValue());
						handler.sendEmptyMessage(EventType.JOIN_IM_GROUP_FAIL);
					}
				});
	}

}
