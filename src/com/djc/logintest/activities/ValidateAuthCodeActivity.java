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
import android.widget.TextView;
import android.widget.Toast;

import com.djc.logintest.R;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.handler.MyHandler;
import com.djc.logintest.taskmgr.AuthCodeCountDownTask;
import com.djc.logintest.taskmgr.BindPushTask;
import com.djc.logintest.taskmgr.GetAuthCodeTask;
import com.djc.logintest.taskmgr.ValidateAuthCodeTask;
import com.djc.logintest.utils.Utils;

public class ValidateAuthCodeActivity extends MyActivity {
    private Handler handler;
    private ProgressDialog dialog;
    private EditText inuputAuthCodeView;
    private Button sendAuthCodeBtn;
    private TextView aucodeContentView;
    private Button getAuthCodeBtn;
    private String phoneNum;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authcode);
        initView();
        initHandler();
        runAuthCodeCountDownTask();
    }

    private void initView() {
        initProgressDlg();
        initTextView();
        initEditView();
        initBtn();
    }

    private void initEditView() {
        inuputAuthCodeView = (EditText) findViewById(R.id.inuputAuthCodeView);
        // test
        inuputAuthCodeView.setText("116548");
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
                getAuthCodeBtn.setEnabled(false);
                runGetAuthCodeTask();
            }
        });
        getAuthCodeBtn.setEnabled(false);
    }

    private void initSendAuthCodeBtn() {
        sendAuthCodeBtn = (Button) findViewById(R.id.sendAuthCodeBtn);
        sendAuthCodeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.checkAuthCode(getPhoneNum())) {
                    runValidateAuthCodeTask();
                } else {
                    Utils.showSingleBtnEventDlg(EventType.AUTH_CODE_INPUT_ERROR,
                            ValidateAuthCodeActivity.this);
                }
            }
        });
    }

    private void runAuthCodeCountDownTask() {
        // 禁止不停的发起获取验证码的操作，默认1分钟后才能重新获取
        new AuthCodeCountDownTask(handler, ConstantValue.TIME_LIMIT_TO_GET_AUTHCODE_AGAIN)
                .execute();
    }

    private void runGetAuthCodeTask() {
        dialog.setMessage(getResources().getString(R.string.getting_auth_code));
        dialog.show();
        new GetAuthCodeTask(handler, phoneNum, ConstantValue.TYPE_GET_REG_AUTHCODE).execute();
    }

    private void runValidateAuthCodeTask() {
        dialog.setMessage(getResources().getString(R.string.validate_phone_num));
        dialog.show();
        new ValidateAuthCodeTask(handler, phoneNum, getAuthcode()).execute();
    }

    private void initProgressDlg() {
        dialog = new ProgressDialog(this);
        dialog.setMessage(getResources().getString(R.string.validate_phone_num));
        dialog.setCancelable(false);
    }

    private void initTextView() {
        aucodeContentView = (TextView) findViewById(R.id.aucodeContentView);
        phoneNum = getIntent().getStringExtra(JSONConstant.PHONE_NUM);
        aucodeContentView.setText(String.format(getResources().getString(R.string.aucodeContent),
                phoneNum));
    }

    private String getAuthcode() {
        return inuputAuthCodeView.getText().toString();
    }

    public String getPhoneNum() {
        String phonenum = inuputAuthCodeView.getText().toString();
        return phonenum;
    }

    private void initHandler() {
        handler = new MyHandler(this, dialog) {
            @Override
            public void handleMessage(Message msg) {
                if (ValidateAuthCodeActivity.this.isFinishing()) {
                    Log.w("djc", "do nothing when activity finishing!");
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
                    handleGetAuthCodeFail();
                    break;

                case EventType.AUTHCODE_COUNTDOWN_GO:
                    handleCountDownGo(msg.arg1);
                    break;
                case EventType.AUTHCODE_COUNTDOWN_OVER:
                    handleCountDownOver();
                    break;
                case EventType.BIND_SUCCESS:
                    handleBindSuccess();
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

    public void handleBindSuccess() {
        // PushModel.getPushModel().setSchollIDAsTag();
        startToTransitActivity();
    }

    protected void handleAuthCodeSuccess() {
        runBindPushTask();
    }

    private void runBindPushTask() {
        new BindPushTask(handler, phoneNum).execute();
    }

    private void startToTransitActivity() {
        Log.d("DJC DDD", "startToTransitActivity");
        getAuthCodeBtn.setEnabled(false);
        sendAuthCodeBtn.setEnabled(false);
        Intent intent = new Intent();
        intent.setClass(this, TransitActivity.class);
        startActivity(intent);
        finish();
    }

    private void handleGetAuthCodeFail() {
        Utils.showSingleBtnEventDlg(EventType.NET_WORK_INVALID, ValidateAuthCodeActivity.this);
        getAuthCodeBtn.setEnabled(true);
    }

    private void handleGetAuthCodeSuccess() {
        Toast.makeText(this, R.string.getAuthCodeSuccess, Toast.LENGTH_LONG).show();
        runAuthCodeCountDownTask();
    }

    private void handleCountDownOver() {
        getAuthCodeBtn.setText(getResources().getString(R.string.getAuthCode));
        getAuthCodeBtn.setEnabled(true);
    }

    private void handleCountDownGo(int second) {
        getAuthCodeBtn.setText(String.format(getResources()
                .getString(R.string.getAuthCodeCountDown), String.valueOf(second)));
    }
}
