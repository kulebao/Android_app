package com.cocobabys.activities;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.cocobabys.R;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.BindedNumInfo;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.net.HttpResult;
import com.cocobabys.utils.Utils;

public class LocationMgrActivity extends TabChildActivity {
	private static final String SEND_SMS_FILTER = "djc_send_sms";
	private ProgressDialog progressDialog;
	private MyHandler handler;
	private SmsManager smsMgr;
	private SmsBroadcastReceiver sendReceiver;
	private List<BindedNumInfo> allBindedNumInfo;
	private BindedNumInfo selectedBindedNumInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.location_mgr);
		initView();
		initHandler();
		initSmsMgr();
	}

	private void initView() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);

		Button doLocBtn = (Button) findViewById(R.id.doLocationBtn);

		doLocBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				allBindedNumInfo = DataMgr.getInstance().getAllBindedNumInfo();
				if (allBindedNumInfo.isEmpty()) {
					Utils.showSingleBtnResDlg(R.string.pls_bind_lbs_num,
							LocationMgrActivity.this);
				} else if (allBindedNumInfo.size() == 1) {
					// 只有1个绑定号码，不要出现选择框
					runLocTast(allBindedNumInfo.get(0).getPhone_num());
				} else {
					showSingleChoiceDlg(allBindedNumInfo);
				}
			}
		});

		Button loadLocationRecordBtn = (Button) findViewById(R.id.loadLocationRecordBtn);

		loadLocationRecordBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startToRecordActivity();
			}
		});
	}

	private void showSingleChoiceDlg(List<BindedNumInfo> infos) {
		String[] items = getItems(infos);
		new AlertDialog.Builder(this)
				.setTitle(R.string.choose_lbs_num)
				.setSingleChoiceItems(items, 0,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Log.d("showSingleChoiceDlg", "whichButton ="
										+ whichButton);
								selectedBindedNumInfo = allBindedNumInfo
										.get(whichButton);
								Log.d("showSingleChoiceDlg", "bindedNumInfo ="
										+ selectedBindedNumInfo);
							}
						})
				.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								runLocTast(selectedBindedNumInfo.getPhone_num());
							}
						})
				.setNegativeButton(R.string.back,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						}).create().show();
	}

	private String[] getItems(List<BindedNumInfo> infos) {
		String[] arrays = new String[infos.size()];

		for (int i = 0; i < infos.size(); i++) {
			arrays[i] = infos.get(i).toString();
		}

		return arrays;
	}

	protected void startToRecordActivity() {
		Intent intent = new Intent();
		intent.setClass(this, LocationRecordActivity.class);
		startActivity(intent);
	}

	private void initHandler() {
		handler = new MyHandler(this, progressDialog) {
			@Override
			public void handleMessage(Message msg) {
				if (LocationMgrActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				default:
					break;
				}
			}
		};
	}

	private void runLocTast(final String lbsNum) {
		Utils.showTwoBtnResDlg(R.string.sendSmsNotice, this,
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								testLocation();
							}
						}).start();
						// progressDialog.setMessage(getResources().getString(
						// R.string.sendingLocCommand));
						// progressDialog.show();
						// send_sms();
					}
				});
	}

	public void initSmsMgr() {
		smsMgr = SmsManager.getDefault();
		// 短信发送通知，结果是发送成功或失败
		IntentFilter sendIntentFilter = new IntentFilter(SEND_SMS_FILTER);
		sendReceiver = new SmsBroadcastReceiver();
		this.registerReceiver(sendReceiver, sendIntentFilter);

		// 短信接收通知，结果是目标手机是否接收成功，暂不需要
		// IntentFilter deliverIntentFilter = new IntentFilter("deliver_sms");
		// SmsBroadcastReceiver deliverReceiver = new SmsBroadcastReceiver();
		// this.registerReceiver(deliverReceiver, deliverIntentFilter);
	}

	private class SmsBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			progressDialog.cancel();
			int resultCode = getResultCode();
			Log.d("DDD resultCode", "resultCode =" + resultCode + "  action="
					+ intent.getAction());
			switch (resultCode) {
			case Activity.RESULT_OK:
				// 短信发送成功
				Utils.showSingleBtnResDlg(R.string.sendsmsSuccess,
						LocationMgrActivity.this);
				break;
			default:
				Utils.showSingleBtnResDlg(R.string.sendsmsfail,
						LocationMgrActivity.this);
				break;
			}

			// 短信接收通知,暂不需要
			// if (intent.getAction() == "deliver_sms") {
			// }
		}
	}

	private void send_sms() {
		String destinationAddress = "13540076909";
		String text = "##777*";

		Intent sIntent = new Intent(SEND_SMS_FILTER);
		// 短信成功发送后才发送该广播
		PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, sIntent,
				0);

		smsMgr.sendTextMessage(destinationAddress, null, text, sentIntent, null);
	}

	@Override
	protected void onDestroy() {
		this.unregisterReceiver(sendReceiver);
		super.onDestroy();
	}

	protected void testLocation() {
		// 有时候收不到通知，这里bind一下试试
		// PushModel.getPushModel().bind();
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(JSONConstant.ACCOUNT_NAME, Utils.getAccount());
			jsonObject.put(JSONConstant.LATITUDE, "30.650387");
			jsonObject.put(JSONConstant.LONGITUDE, "104.040387");
			String command = jsonObject.toString();
			Log.d("DDD TEST", "str =" + command);
			HttpResult result = HttpClientHelper.executePost(
					ServerUrls.LOCATION, command);
			Log.d("DDD TEST", "result =" + result.getResCode());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
