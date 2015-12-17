package com.cocobabys.activities;

import com.cocobabys.R;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.taskmgr.BindPushTask;
import com.cocobabys.taskmgr.LoginTask;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

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
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends MyActivity {
	private EditText inuputpwdView;
	private Button loginBtn;
	private String phoneunm;
	private Handler handler;
	private ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		initDialog();
		initHandler();
		initView();
	}

	private void initDialog() {
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage(getResources().getString(R.string.loginning));
	}

	private void initHandler() {

		handler = new MyHandler(this, dialog) {

			@Override
			public void handleMessage(Message msg) {
				if (LoginActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}

				Log.w("dasd", "LoginActivity msg.what=" + msg.what);
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.LOGIN_SUCCESS:
					handleLoginSuccess();
					break;
				case EventType.PWD_INCORRECT:
					handlePWDIncorrect();
					break;
				case EventType.BIND_SUCCESS:
					handBindSuccess();
					break;
				case EventType.BIND_FAILED:
					// 绑定失败，删除登录成功时保存的用户信息
					DataUtils.clearProp();
					Utils.showSingleBtnEventDlg(EventType.BIND_FAILED,
							LoginActivity.this);
					break;
				case EventType.PHONE_NUM_IS_INVALID:
					DataUtils.clearProp();
					Utils.showSingleBtnEventDlg(EventType.PHONE_NUM_IS_INVALID,
							LoginActivity.this);
					break;
				case EventType.PHONE_NUM_IS_ALREADY_LOGIN:
					DataUtils.clearProp();
					Utils.showSingleBtnEventDlg(
							EventType.PHONE_NUM_IS_ALREADY_LOGIN,
							LoginActivity.this);
					break;
				case EventType.SERVER_BUSY:
					// 绑定失败，删除登录成功时保存的用户信息
					DataUtils.clearProp();
					Toast.makeText(LoginActivity.this, "服务器忙，请稍后再试，谢谢！",
							Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
			}
		};
	}

	public void handBindSuccess() {
		startToMainActivity();
	}

	protected void handleLoginSuccess() {
		new BindPushTask(handler, phoneunm).execute();
	}

	private void startToMainActivity() {
		Intent intent = new Intent();
		intent.setClass(this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	public void handlePWDIncorrect() {
		Utils.showSingleBtnEventDlg(EventType.PWD_INCORRECT, LoginActivity.this);
		inuputpwdView.setText("");
	}

	private void initView() {
		inuputpwdView = (EditText) findViewById(R.id.inuputpwdView);
		TextView accountView = (TextView) findViewById(R.id.accountView);

		phoneunm = getIntent().getStringExtra(JSONConstant.PHONE_NUM);
		String text = String.format(
				getResources().getString(R.string.login_notice), phoneunm);
		accountView.setText(text);

		initLoginBtn();
		initForgetPWDView();
		initChooseAnotherAccountView();
	}

	private void initChooseAnotherAccountView() {
		TextView chooseAnotherAccountView = (TextView) findViewById(R.id.choose_another_account);
		chooseAnotherAccountView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startValidatePhoneNumActivity();
			}
		});
	}

	protected void startValidatePhoneNumActivity() {
		Intent intent = new Intent();
		intent.setClass(this, ValidatePhoneNumActivity.class);
		startActivity(intent);
		finish();
	}

	public void initForgetPWDView() {
		TextView forgetPwdView = (TextView) findViewById(R.id.forgetPwdView);
		forgetPwdView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startResetPWDActivity();
			}
		});
	}

	public void initLoginBtn() {
		loginBtn = (Button) findViewById(R.id.loginBtn);
		loginBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Utils.checkPWD(getPwd())) {
					// 发起登录
					Utils.closeKeyBoard(LoginActivity.this);
					runLoginTask();
				} else {
					Utils.showSingleBtnEventDlg(EventType.PWD_FORMAT_ERROR,
							LoginActivity.this);
				}
			}
		});
	}

	private void runLoginTask() {
		dialog.show();
		new LoginTask(handler, phoneunm, getPwd()).execute();
	}

	private void startResetPWDActivity() {
		Bundle bundle = new Bundle();
		bundle.putString(JSONConstant.PHONE_NUM, phoneunm);
		Intent intent = new Intent();
		intent.putExtras(bundle);
		intent.setClass(this, ResetPWDActivity.class);
		startActivity(intent);
	}

	private String getPwd() {
		String pwd = inuputpwdView.getText().toString();
		return pwd;
	}
}
