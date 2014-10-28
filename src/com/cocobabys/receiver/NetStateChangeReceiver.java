package com.cocobabys.receiver;

import java.util.concurrent.TimeUnit;

import com.cocobabys.threadpool.MyThreadPoolMgr;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.MethodUtils;
import com.cocobabys.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetStateChangeReceiver extends BroadcastReceiver {
	private static int currentNetWorkType = Utils.NETWORK_NOT_CONNECTED;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (DataUtils.isLoginout()) {
			return;
		}

		String action = intent.getAction();
		Log.d("DDD", "NetStateChangeReceiver onReceive action=" + action);
		int connectedType = Utils.getConnectedType(context);
		Log.d("DDD", "NetStateChangeReceiver connectedType=" + connectedType
				+ "  currentNetWorkType=" + currentNetWorkType);

		if (Utils.isNetworkConnected(context)
				&& currentNetWorkType == Utils.NETWORK_NOT_CONNECTED) {
			Log.d("DDD", "onReceive NetStateChangeReceiver");
			MethodUtils.executeCheckNewsCommand(context);
//			runCheckBindTask();
		}

		currentNetWorkType = connectedType;
	}

	// private void runCheckBindTask() {
	// MyThreadPoolMgr.getGenericService().execute(new Runnable() {
	// @Override
	// public void run() {
	// Utils.bindPush();
	// }
	// });
	// }
}
