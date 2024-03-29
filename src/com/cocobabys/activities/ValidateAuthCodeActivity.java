package com.cocobabys.activities;

import com.cocobabys.R;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.taskmgr.AuthCodeCountDownTask;
import com.cocobabys.taskmgr.GetAuthCodeTask;
import com.cocobabys.taskmgr.ValidateAuthCodeTask;
import com.cocobabys.utils.Utils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ValidateAuthCodeActivity extends MyActivity {
	private Handler handler;
	private ProgressDialog dialog;
	private EditText inuputAuthCodeView;
	private Button sendAuthCodeBtn;
	private TextView aucodeContentView;
	private Button getAuthCodeBtn;
	private String phoneNum;
	private AsyncTask<Void, Void, Void> authCodeCountDownTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authcode);
		initView();
		initHandler();
	}

	private void initView() {
		initProgressDlg();
		initTextView();
		initEditView();
		initBtn();
	}

	private void initEditView() {
		inuputAuthCodeView = (EditText) findViewById(R.id.inuputAuthCodeView);
	}

	private void initBtn() {
		initSendAuthCodeBtn();
		initGetAuthCodeBtn();
	}

	private void initGetAuthCodeBtn() {
		getAuthCodeBtn = (Button) findViewById(R.id.getAuthCodeBtn);
		getAuthCodeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableGetAuthBtn();
				runGetAuthCodeTask();
			}

		});
	}

	private void initSendAuthCodeBtn() {
		sendAuthCodeBtn = (Button) findViewById(R.id.sendAuthCodeBtn);
		sendAuthCodeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (MyApplication.getInstance().isForTest()
						&& getAuthcode() != null
						&& getAuthcode().startsWith("22")) {
					handleAuthCodeSuccess();
					return;
				}
				if (Utils.checkAuthCode(getAuthcode())) {
					runValidateAuthCodeTask();
				} else {
					Utils.showSingleBtnEventDlg(
							EventType.AUTH_CODE_INPUT_ERROR,
							ValidateAuthCodeActivity.this);
				}
			}
		});
	}

	private void runAuthCodeCountDownTask() {
		authCodeCountDownTask = new AuthCodeCountDownTask(handler,
				ConstantValue.TIME_LIMIT_TO_GET_AUTHCODE_AGAIN).execute();
	}

	private void runGetAuthCodeTask() {
		dialog.setMessage(getResources().getString(R.string.getting_auth_code));
		dialog.show();
		new GetAuthCodeTask(handler, phoneNum,
				ConstantValue.TYPE_GET_REG_AUTHCODE).execute();
	}

	private void runValidateAuthCodeTask() {
		Utils.closeKeyBoard(this);
		dialog.setMessage(getResources().getString(R.string.checking_auth_code));
		dialog.show();
		new ValidateAuthCodeTask(handler, phoneNum, getAuthcode()).execute();
	}

	private void initProgressDlg() {
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
	}

	private void initTextView() {
		aucodeContentView = (TextView) findViewById(R.id.aucodeContentView);
		phoneNum = getIntent().getStringExtra(JSONConstant.PHONE_NUM);
		aucodeContentView.setText(String.format(
				getResources().getString(R.string.aucodeContent), phoneNum));
	}

	private String getAuthcode() {
		return inuputAuthCodeView.getText().toString();
	}

	private void initHandler() {
		handler = new MyHandler(this, dialog) {
			@Override
			public void handleMessage(Message msg) {
				if (ValidateAuthCodeActivity.this.isFinishing()) {
					Log.w("djc",
							"ValidateAuthCodeActivity do nothing when activity finishing!");
					return;
				}
				Log.d("DJC DDD", "handleMessage msg:" + msg.what);
				super.handleMessage(msg);

				switch (msg.what) {
				case EventType.AUTH_CODE_IS_VALID:
					handleAuthCodeSuccess();
					break;

				case EventType.GET_AUTH_CODE_SUCCESS:
					handleGetAuthCodeSuccess();
					break;

				case EventType.GET_AUTH_CODE_FAIL:
					handleGetAuthCodeFail(msg.what);
					break;
				case EventType.GET_AUTH_CODE_TOO_OFTEN:
					handleGetAuthCodeFail(msg.what);
					break;

				case EventType.AUTHCODE_COUNTDOWN_GO:
					handleCountDownGo(msg.arg1);
					break;
				case EventType.AUTHCODE_COUNTDOWN_OVER:
					handleCountDownOver();
					break;
				case EventType.BIND_SUCCESS:
					// handleBindSuccess();
					break;
				case EventType.BIND_FAILED:
					Utils.showSingleBtnEventDlg(EventType.BIND_FAILED,
							ValidateAuthCodeActivity.this);
					break;
				default:
					break;
				}
			}

		};
	}

	protected void handleAuthCodeSuccess() {
		startLoginActivity();
	}

	private Intent createIntent() {
		Bundle bundle = new Bundle();
		bundle.putString(JSONConstant.PHONE_NUM, phoneNum);

		Intent intent = new Intent();
		intent.putExtras(bundle);
		return intent;
	}

	private void startLoginActivity() {
		// 防止启动到下一个界面前，btn又被点击
		Intent intent = createIntent();
		intent.setClass(this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	private void handleGetAuthCodeFail(int eventtype) {
		Utils.showSingleBtnEventDlg(eventtype, ValidateAuthCodeActivity.this);
		enableGetAuthBtn();
	}

	private void disableGetAuthBtn() {
		getAuthCodeBtn.setTextColor(getResources().getColor(R.color.dark_gray));
		getAuthCodeBtn.setBackgroundResource(R.drawable.small_btn);
		getAuthCodeBtn.setEnabled(false);
	}

	private void enableGetAuthBtn() {
		getAuthCodeBtn.setEnabled(true);
		getAuthCodeBtn.setBackgroundResource(R.drawable.small_btn);
		getAuthCodeBtn.setTextColor(getResources().getColor(R.color.white));
	}

	private void handleGetAuthCodeSuccess() {
		Toast.makeText(this, R.string.getAuthCodeSuccess, Toast.LENGTH_SHORT)
				.show();
		runAuthCodeCountDownTask();
	}

	private void handleCountDownOver() {
		getAuthCodeBtn.setText(getResources().getString(R.string.getAuthCode));
		enableGetAuthBtn();
	}

	private void handleCountDownGo(int second) {
		getAuthCodeBtn.setText(String.format(
				getResources().getString(R.string.getAuthCodeCountDown),
				String.valueOf(second)));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (authCodeCountDownTask != null) {
			authCodeCountDownTask.cancel(true);
		}
	}

}
