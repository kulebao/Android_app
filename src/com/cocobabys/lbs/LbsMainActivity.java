package com.cocobabys.lbs;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.baidu.mapapi.SDKInitializer;
import com.cocobabys.R;
import com.cocobabys.utils.Utils;

public class LbsMainActivity extends Activity {

	private SDKReceiver mReceiver;

	public class SDKReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String s = intent.getAction();
			Log.d("", "action: " + s);
			if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
				Utils.makeToast(LbsMainActivity.this, "key 验证出错! 请在 AndroidManifest.xml 文件中检查 key 设置");
			} else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
				Utils.makeToast(LbsMainActivity.this, "网络出错");
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lbs_activity_main);
		initReceiver();
	}

	private void initReceiver() {
		// 注册 SDK 广播监听者
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
		iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
		mReceiver = new SDKReceiver();
		registerReceiver(mReceiver, iFilter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 取消监听 SDK 广播
		unregisterReceiver(mReceiver);
	}

	public void track(View v) {
		Intent intent = new Intent(this, LbsLocation.class);
		startActivity(intent);
	}

	public void route(View v) {
		Intent intent = new Intent(this, LbsTrack.class);
		startActivity(intent);
	}

	public void offlineMap(View v) {
		Intent intent = new Intent(this, LbsOfflineMapList.class);
		startActivity(intent);
	}

	public void fence(View v) {
		Intent intent = new Intent(this, LbsFence.class);
		startActivity(intent);
	}
}
