package com.cocobabys.lbs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.cocobabys.R;
import com.cocobabys.activities.UmengStatisticsActivity;
import com.cocobabys.bean.LocatorPower;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.GetLocatorPowerJob;
import com.cocobabys.net.LbsMethod;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

public class LbsMainActivity extends UmengStatisticsActivity {

	private SDKReceiver mReceiver;
	private MyHandler handler;
	private TextView powerView;
	private GetLocatorPowerJob getLocatorPowerJob;
	private EditText inputView;

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
		initViews();

		initReceiver();
		initHandler();
		runGetPowerTask();
	}

	private void initViews() {
		powerView = (TextView) findViewById(R.id.power);
		inputView = (EditText) findViewById(R.id.inputid);

		String deviceid = DataUtils.getProp(ConstantValue.LOCATOR_ID);
		if (TextUtils.isEmpty(deviceid)) {
			inputView.setText(LbsMethod.FAKE_DEVICE);
		} else {
			inputView.setText(deviceid);
		}
	}

	private void runGetPowerTask() {
		getLocatorPowerJob = new GetLocatorPowerJob(handler);
		getLocatorPowerJob.execute();
	}

	private void initHandler() {
		handler = new MyHandler(LbsMainActivity.this) {
			@Override
			public void handleMessage(Message msg) {
				if (LbsMainActivity.this.isFinishing()) {
					Log.w("TrackDemoDJC isFinishing", "handleMessage donothing msg=" + msg.what);
					return;
				}

				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.GET_LOCATOR_POWER_SUCCESS:
					LocatorPower power = (LocatorPower) msg.obj;
					Log.d("handleMessage", "aaa =" + power.getPower_level());
					powerView.setText(Utils.getResString(R.string.lbs_power) + power.getPower_level() + "%");
					break;
				case EventType.GET_LOCATOR_POWER_FAIL:
					Utils.makeToast(LbsMainActivity.this, Utils.getResString(R.string.lbs_get_power_fail));
					break;
				default:
					break;
				}
			}
		};
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
		if (getLocatorPowerJob != null) {
			getLocatorPowerJob.cancel(true);
		}
	}

	public void confirm(View v) {
		if (TextUtils.isEmpty(inputView.getText())) {
			Utils.makeToast(this, "设备id不能为空");
			return;
		}

		DataUtils.saveProp(ConstantValue.LOCATOR_ID, inputView.getText().toString());
		Utils.makeToast(this, "设备id保存成功");
	}

	public void track(View v) {
		if (TextUtils.isEmpty(DataUtils.getProp(ConstantValue.LOCATOR_ID))) {
			Utils.makeToast(this, "设备id不能为空");
			return;
		}

		Intent intent = new Intent(this, LbsLocation.class);
		startActivity(intent);
	}

	public void route(View v) {
		if (TextUtils.isEmpty(DataUtils.getProp(ConstantValue.LOCATOR_ID))) {
			Utils.makeToast(this, "设备id不能为空");
			return;
		}
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
