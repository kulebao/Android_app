package com.djc.logintest.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.djc.logintest.R;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.handler.MyHandler;
import com.djc.logintest.taskmgr.AuthCodeCountDownTask;
import com.djc.logintest.taskmgr.GetAuthCodeTask;
import com.djc.logintest.taskmgr.ResetPWDTask;
import com.djc.logintest.utils.Utils;

public class ResetPWDActivity extends UmengStatisticsActivity {
	private Handler handler;
	private Button getAuthCodeBtn;
	private ProgressDialog dialog;
	private String phonenum;
	private EditText inputAuthCodeView;
	private EditText inuputPWDView;
	private EditText reInuputPWDView;
	private Button resetPWDBtn;
	private AsyncTask<Void, Void, Void> authCodeCountDownTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resetpwd);
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		phonenum = getIntent().getStringExtra(JSONConstant.PHONE_NUM);
		initHandler();
		initView();
	}

	private void handleCountDownGo(int second) {
		getAuthCodeBtn.setText(String.format(
				getResources().getString(R.string.getAuthCodeCountDown),
				String.valueOf(second)));
	}

	private void initHandler() {
		handler = new MyHandler(this, dialog) {
			@Override
			public void handleMessage(Message msg) {
				if (ResetPWDActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				Log.w("DDDDDD", "ResetPWDActivity event =" + msg.what);
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.RESET_PWD_SUCCESS:
					handleResetPwdSuccess();
					break;
				case EventType.RESET_PWD_FAILED:
					Utils.showSingleBtnResDlg(R.string.change_pwd_failed,
							ResetPWDActivity.this);
					break;

				case EventType.GET_AUTH_CODE_SUCCESS:
					Toast.makeText(ResetPWDActivity.this,
							R.string.getAuthCodeSuccess, Toast.LENGTH_LONG)
							.show();
					runAuthCodeCountDownTask();
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
				default:
					break;
				}
			}

		};
	}

	private void initView() {
		getAuthCodeBtn = (Button) findViewById(R.id.getAuthCodeBtn);
		getAuthCodeBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				disableGetAuthBtn();
				runGetAuthCodeTask();
			}

		});
		resetPWDBtn = (Button) findViewById(R.id.resetPWDBtn);
		resetPWDBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (checkInput()) {
					// 发起密码重置
					Log.w("djc", "runResetPWDTask!");
					runResetPWDTask();
				}
			}
		});

		inputAuthCodeView = (EditText) findViewById(R.id.inputAuthCodeView);

		inuputPWDView = (EditText) findViewById(R.id.inuputPWDView);
		reInuputPWDView = (EditText) findViewById(R.id.reInuputPWDView);
	}

	private void disableGetAuthBtn() {
		getAuthCodeBtn.setTextColor(getResources().getColor(R.color.dark_gray));
		getAuthCodeBtn.setBackgroundResource(R.drawable.normal_btn_pressed);
		getAuthCodeBtn.setEnabled(false);
	}

	private void enableGetAuthBtn() {
		getAuthCodeBtn.setEnabled(true);
		getAuthCodeBtn.setBackgroundResource(R.drawable.normal_btn);
		getAuthCodeBtn.setTextColor(getResources().getColor(R.color.white));
	}

	private void runResetPWDTask() {
		try {
			
			//907024 后门验证码
			new ResetPWDTask(handler, phonenum, getAuthCode(), getPwd())
					.execute();
			// ResetPWDJob resetPWDJob = new ResetPWDJob(handler, phonenum,
			// getAuthCode(), getPwd());
			// MyThreadPoolMgr.getGenericService().submit(resetPWDJob);

			dialog.setMessage(getResources().getString(R.string.pwdreseting));
			dialog.show();
		} catch (Exception e) {
			dialog.cancel();
			e.printStackTrace();
		}
	}

	private boolean checkInput() {
		if (!Utils.checkAuthCode(getAuthCode())) {
			Utils.showSingleBtnEventDlg(EventType.AUTH_CODE_INPUT_ERROR,
					ResetPWDActivity.this);
			return false;
		}

		if (!Utils.checkPWD(getPwd())) {
			Utils.showSingleBtnEventDlg(EventType.PWD_FORMAT_ERROR,
					ResetPWDActivity.this);
			clearInputView();
			return false;
		}

		if (!isTwoPwdSame()) {
			Utils.showSingleBtnResDlg(R.string.pwd_confirm_error,
					ResetPWDActivity.this);
			clearInputView();
			return false;
		}

		return true;
	}

	private void clearInputView() {
		inuputPWDView.setText("");
		reInuputPWDView.setText("");
	}

	// 判断2次密码输入是否一致
	private boolean isTwoPwdSame() {
		return inuputPWDView.getText().toString()
				.equals(reInuputPWDView.getText().toString());
	}

	private String getPwd() {
		return inuputPWDView.getText().toString();
	}

	private String getAuthCode() {
		return inputAuthCodeView.getText().toString();
	}

	private void handleResetPwdSuccess() {
		Utils.showSingleBtnResDlg(R.string.reset_pwd_success, this,
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ResetPWDActivity.this.finish();
					}
				});
	}

	private void handleGetAuthCodeFail(int eventtype) {
		Utils.showSingleBtnEventDlg(eventtype, ResetPWDActivity.this);
		enableGetAuthBtn();
	}

	private void runGetAuthCodeTask() {
		dialog.setMessage(getResources().getString(R.string.getting_auth_code));
		dialog.show();
		new GetAuthCodeTask(handler, phonenum,
				ConstantValue.TYPE_GET_RESER_PWD_AUTHCODE).execute();
	}

	private void runAuthCodeCountDownTask() {
		authCodeCountDownTask = new AuthCodeCountDownTask(handler,
				ConstantValue.TIME_LIMIT_TO_GET_AUTHCODE_AGAIN).execute();
	}

	private void handleCountDownOver() {
		getAuthCodeBtn.setText(getResources().getString(R.string.getAuthCode));
		enableGetAuthBtn();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (authCodeCountDownTask != null) {
			authCodeCountDownTask.cancel(true);
		}
	}

}
