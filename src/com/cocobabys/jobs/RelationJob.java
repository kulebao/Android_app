package com.cocobabys.jobs;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ChildInfo;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.net.ChildMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

public class RelationJob extends MyJob {
	private Handler handler;
	private List<ParentInfo> parentInfos = new ArrayList<ParentInfo>();
	private boolean bSuccess = false;

	public RelationJob(Handler handler) {
		this.handler = handler;
	}

	@Override
	public void run() {
		try {
			getSelectedChildRelationShip();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Message msg = Message.obtain();
			msg.what = bSuccess ? EventType.GET_RELATIONSHIP_SUCCESS : EventType.GET_RELATIONSHIP_FAIL;
			msg.obj = parentInfos;
			handler.sendMessage(msg);
		}

	}

	private void getSelectedChildRelationShip() throws Exception {
		ChildInfo childInfo = DataMgr.getInstance().getSelectedChild();
		MethodResult bret = getResult(childInfo.getServer_id());
		if (bret.getResultType() == EventType.GET_RELATIONSHIP_SUCCESS) {
			bSuccess = true;
			@SuppressWarnings("unchecked")
			List<ParentInfo> list = (List<ParentInfo>) bret.getResultObj();

			for (ParentInfo parentInfo : list) {
				if (!parentInfos.contains(parentInfo)) {
					parentInfos.add(parentInfo);
				}
			}
		}
	}

	private void getAllChildRelationShip() throws Exception {
		List<ChildInfo> allChildrenInfo = DataMgr.getInstance().getAllChildrenInfo();

		for (ChildInfo childInfo : allChildrenInfo) {
			MethodResult bret = getResult(childInfo.getServer_id());
			if (bret.getResultType() == EventType.GET_RELATIONSHIP_SUCCESS) {
				bSuccess = true;
				@SuppressWarnings("unchecked")
				List<ParentInfo> list = (List<ParentInfo>) bret.getResultObj();

				for (ParentInfo parentInfo : list) {
					if (!parentInfos.contains(parentInfo)) {
						parentInfos.add(parentInfo);
					}
				}
			}
		}
	}

	private MethodResult getResult(final String child_id) throws Exception {
		MyProxy proxy = new MyProxy();
		MyProxyImpl bind = (MyProxyImpl) proxy.bind(new MyProxyImpl() {
			@Override
			public MethodResult handle() throws Exception {
				MethodResult result = ChildMethod.getMethod().getRelationshipByChild(child_id);
				return result;
			}
		});

		MethodResult bret = MethodUtils.getBindResult(bind);
		return bret;
	}
}
