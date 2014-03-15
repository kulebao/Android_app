package com.djc.logintest.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.djc.logintest.R;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.handler.MyHandler;
import com.djc.logintest.taskmgr.ValidatePhoneNumTask;
import com.djc.logintest.utils.Utils;

public class ValidatePhoneNumActivity extends MyActivity {
	private Button sendPhoneNumBtn;
	private ProgressDialog dialog;
	private Handler handler;
	private EditText inuputnumView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checkphone);
		initDialog();
		initView();
		initHandler();
	}

	private void initHandler() {
		handler = new MyHandler(this, dialog) {
			@Override
			public void handleMessage(Message msg) {
				if (ValidatePhoneNumActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);

				switch (msg.what) {
				case EventType.PHONE_NUM_IS_INVALID:
					Utils.showSingleBtnEventDlg(EventType.PHONE_NUM_IS_INVALID,
							ValidatePhoneNumActivity.this);
					break;
				case EventType.PHONE_NUM_IS_FIRST_USE:
					// 首次使用，进入获取验证码界面,目前短信通道断了，直接进入登录
					startAuthCodeActivity();
					// startLoginActivity();
					break;
				case EventType.PHONE_NUM_IS_ALREADY_BIND:
					// 已经绑定，直接进入登录界面
					// startLoginActivity();
					startAuthCodeActivity();
					break;
				default:
					break;
				}
			}
		};
	}

	private void startLoginActivity() {
		// 防止启动到下一个界面前，btn又被点击
		sendPhoneNumBtn.setEnabled(false);
		Intent intent = createIntent();
		intent.setClass(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	private void startAuthCodeActivity() {
		// 防止启动到下一个界面前，btn又被点击
		sendPhoneNumBtn.setEnabled(false);
		Intent intent = createIntent();
		intent.setClass(this, ValidateAuthCodeActivity.class);
		startActivity(intent);
		finish();
	}

	private Intent createIntent() {
		Bundle bundle = new Bundle();
		bundle.putString(JSONConstant.PHONE_NUM, inuputnumView.getText()
				.toString());

		Intent intent = new Intent();
		intent.putExtras(bundle);
		return intent;
	}

	private void initView() {
		inuputnumView = (EditText) findViewById(R.id.inuputnumView);
		// test
		inuputnumView.setText("13408654680");

		sendPhoneNumBtn = (Button) findViewById(R.id.sendPhoneNumBtn);
		sendPhoneNumBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Utils.checkPhoneNum(getPhoneNum())) {
					dialog.show();
					runValidatePhoneNumTask();
				} else {
					Utils.showSingleBtnEventDlg(
							EventType.PHONE_NUM_INPUT_ERROR,
							ValidatePhoneNumActivity.this);
				}
			}
		});
	}

	public void initDialog() {
		dialog = new ProgressDialog(this);
		dialog.setMessage(getResources().getString(R.string.validate_phone_num));
		dialog.setCancelable(false);
	}

	public String getPhoneNum() {
		String phonenum = inuputnumView.getText().toString();
		return phonenum;
	}

	private void runValidatePhoneNumTask() {
		new ValidatePhoneNumTask(handler, getPhoneNum()).execute();
	}
}