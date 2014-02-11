package com.djc.logintest.receiver;

import com.djc.logintest.utils.MethodUtils;
import com.djc.logintest.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetStateChangeReceiver extends BroadcastReceiver {
	private static int currentNetWorkType = Utils.NETWORK_NOT_CONNECTED;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.d("DDD", "NetStateChangeReceiver onReceive action=" + action);
		int connectedType = Utils.getConnectedType(context);
		Log.d("DDD", "NetStateChangeReceiver connectedType=" + connectedType
				+ "  currentNetWorkType=" + currentNetWorkType);

		if (Utils.isNetworkConnected(context)
				&& currentNetWorkType == Utils.NETWORK_NOT_CONNECTED) {
			Log.d("DDD", "onReceive NetStateChangeReceiver");
			MethodUtils.executeCheckNewsCommand(context);
		}

		currentNetWorkType = connectedType;
	}
}
