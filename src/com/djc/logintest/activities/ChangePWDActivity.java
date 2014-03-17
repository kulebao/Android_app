package com.djc.logintest.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.djc.logintest.taskmgr.ChangePWDTask;
import com.djc.logintest.utils.Utils;

public class ChangePWDActivity extends UmengStatisticsActivity {
    private Handler handler;
    private ProgressDialog dialog;
    private EditText inputOldPwdView;
    private EditText inuputNewPWDView;
    private EditText reInuputNewPWDView;
    private Button changePWDBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepwd);
        ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.change_pwd);
        initProgressDlg();
        initHandler();
        initView();
    }

    public void initProgressDlg() {
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage(getResources().getString(R.string.pwdreseting));
    }

    private void initHandler() {
        handler = new MyHandler(this, dialog) {

            @Override
            public void handleMessage(Message msg) {
                if (ChangePWDActivity.this.isFinishing()) {
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                super.handleMessage(msg);
                switch (msg.what) {
                case EventType.CHANGE_PWD_SUCCESS:
                    handleChangePwdSuccess();
                    break;
                case EventType.OLD_PWD_NOT_EQUAL:
                    Utils.showSingleBtnResDlg(R.string.old_pwd_not_equal, ChangePWDActivity.this);
                    break;

                default:
                    break;
                }
            }
        };
    }

    private void initView() {
        changePWDBtn = (Button) findViewById(R.id.changePWDBtn);
        changePWDBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (checkInput()) {
                    // 发起密码重置
                    runResetPWDTask();
                }
            }
        });

        inputOldPwdView = (EditText) findViewById(R.id.inputOldPwdView);
        inuputNewPWDView = (EditText) findViewById(R.id.inuputNewPWDView);
        reInuputNewPWDView = (EditText) findViewById(R.id.reInuputNewPWDView);
    }

    private void runResetPWDTask() {
        dialog.show();
        String phonenum = Utils.getProp(JSONConstant.ACCOUNT_NAME);
        new ChangePWDTask(handler, phonenum, getOldPwd(), getNewPwd()).execute();
    }

    private boolean checkInput() {
        if (!Utils.checkPWD(getOldPwd())) {
            Utils.showSingleBtnEventDlg(EventType.OLD_PWD_FORMAT_ERROR, ChangePWDActivity.this);
            return false;
        }

        if (!Utils.checkPWD(getNewPwd())) {
            Utils.showSingleBtnEventDlg(EventType.NEW_PWD_FORMAT_ERROR, ChangePWDActivity.this);
            clearInputView();
            return false;
        }

        if (!isTwoPwdSame()) {
            Utils.showSingleBtnResDlg(R.string.pwd_confirm_error, ChangePWDActivity.this);
            clearInputView();
            return false;
        }

        return true;
    }

    private void clearInputView() {
        inuputNewPWDView.setText("");
        reInuputNewPWDView.setText("");
    }

    // 判断2次密码输入是否一致
    private boolean isTwoPwdSame() {
        return inuputNewPWDView.getText().toString()
                .equals(reInuputNewPWDView.getText().toString());
    }

    private String getNewPwd() {
        return inuputNewPWDView.getText().toString();
    }

    private String getOldPwd() {
        return inputOldPwdView.getText().toString();
    }

    private void handleChangePwdSuccess() {
        Utils.showSingleBtnResDlg(R.string.change_pwd_success, this,
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ChangePWDActivity.this.finish();
                    }
                });
    }

}
