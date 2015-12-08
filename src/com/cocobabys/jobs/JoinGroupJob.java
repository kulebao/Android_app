package com.cocobabys.jobs;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.OperationCallback;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.util.Log;

import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.info.IMGroupInfo;
import com.cocobabys.net.IMMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;

public class JoinGroupJob extends MyJob {

	private Handler handler;
	private List<String> classidList;
	private CountDownLatch countDownLatch;
	private MethodResult bret = new MethodResult(EventType.JOIN_IM_GROUP_FAIL);

	public JoinGroupJob(Handler handler, List<String> classidList) {
		this.handler = handler;
		this.classidList = classidList;
		countDownLatch = new CountDownLatch(classidList.size());
	}

	@Override
	public void run() {
		try {
			MyProxy proxy = new MyProxy();
			MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
				@Override
				public MethodResult handle() throws Exception {
					try {
						for (String classid : classidList) {
							MethodResult result = IMMethod.getMethod().getGroupInfo(classid);
							if (result.getResultType() == EventType.GET_IM_GROUP_SUCCESS) {
								joinGroup((IMGroupInfo) result.getResultObj());
							} else {
								countDownLatch.countDown();
							}
						}

						countDownLatch.await(30, TimeUnit.SECONDS);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						handler.sendEmptyMessage(bret.getResultType());
					}

					return null;
				}
			});
			bind.handle();

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
						// handler.sendEmptyMessage(EventType.JOIN_IM_GROUP_SUCCESS);
						bret.setResultType(EventType.JOIN_IM_GROUP_SUCCESS);
						countDownLatch.countDown();
					}

					@Override
					public void onError(ErrorCode errorCode) {
						Log.d("", "DDD JOIN_IM_GROUP_FAIL err :" + errorCode.getMessage() + " code="
								+ errorCode.getValue());
						// handler.sendEmptyMessage(EventType.JOIN_IM_GROUP_FAIL);
						countDownLatch.countDown();
					}
				});
	}

}
