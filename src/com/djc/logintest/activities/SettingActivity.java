package com.djc.logintest.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.djc.logintest.R;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.customview.SwitchButton;
import com.djc.logintest.customview.SwitchButton.OnCheckedChangeListener;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.handler.MyHandler;
import com.djc.logintest.taskmgr.CheckUpdateTask;
import com.djc.logintest.utils.Utils;

public class SettingActivity extends UmengStatisticsActivity {
    private Handler handler;
    private ProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.setting);
        initView();
        initDialog();
        initHandler();
    }

    private void initView() {
        initSwitch();

        Button exitLogin = (Button) findViewById(R.id.exitLogin);
        exitLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showTwoBtnResDlg(R.string.confirm_exit, SettingActivity.this,
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handleExitLogin();
                            }

                        });
            }
        });

        Button changePWDBtn = (Button) findViewById(R.id.changePWD);
        changePWDBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startToChangePWDActivity();
            }
        });

        Button checkVersionBtn = (Button) findViewById(R.id.checkVersion);
        checkVersionBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                runCheckUpdateTask();
            }
        });

        Button bindLocationBtn = (Button) findViewById(R.id.bindLocation);
        bindLocationBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startToBindLocationMgrActivity();
            }
        });

        Button changeChildBtn = (Button) findViewById(R.id.changeChild);
        changeChildBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startToSelectChildActivity();
            }
        });

        Button userResponse = (Button) findViewById(R.id.userResponse);
        userResponse.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startToFeedBackActivity();
            }
        });
        Button about_cocobabys = (Button) findViewById(R.id.about_cocobabys);
        about_cocobabys.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startToAboutUsActivity();
            }
        });

    }

    protected void startToAboutUsActivity() {
        Intent intent = new Intent();
        intent.setClass(this, AboutUsActivity.class);
        startActivity(intent);        
    }

    protected void startToFeedBackActivity() {
        Intent intent = new Intent();
        intent.setClass(this, FeedBackActivity.class);
        startActivity(intent);
    }

    public void initSwitch() {
        SwitchButton switchButton = (SwitchButton) findViewById(R.id.voiceswitchbtn);
        switchButton.setChecked(Utils.isVoiceOn());
        switchButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void checkedChange(boolean isChecked) {
                String result = isChecked ? ConstantValue.VOICE_OPEN : ConstantValue.VOICE_OFF;
                Utils.saveProp(ConstantValue.VOICE_CONFIG, result);
                Log.d("DDD", "VOICE_CONFIG  result=" + result);
            }
        });
    }

    private void initDialog() {
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage(getResources().getString(R.string.checking_new_version));
    }

    private void initHandler() {

        handler = new MyHandler(this, dialog) {

            @Override
            public void handleMessage(Message msg) {
                if (SettingActivity.this.isFinishing()) {
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                super.handleMessage(msg);
                switch (msg.what) {
                case EventType.HAS_NEW_VERSION:
                    startToUpdateActivity();
                    break;
                case EventType.HAS_NO_VERSION:
                    Toast.makeText(SettingActivity.this, R.string.no_new_version,
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
                }
            }
        };
    }

    protected void runCheckUpdateTask() {
        // 如果后台在自动更新，这里可能会有冲突，后续需要把后台任务统一管理起来
        dialog.show();
        Utils.saveCheckNewTime(System.currentTimeMillis());
        new CheckUpdateTask(handler, Utils.getAccount(), Utils.getVersionCode()).execute();
    }

    private void handleExitLogin() {
        Utils.clearProp();
        DataMgr.getInstance().upgradeAll();
        Utils.clearSDFolder();
        setResult(ConstantValue.EXIT_LOGIN_RESULT);
        finish();
    }

    private void startToChangePWDActivity() {
        Intent intent = new Intent();
        intent.setClass(this, ChangePWDActivity.class);
        startActivity(intent);
    }

    private void startToUpdateActivity() {
        Intent intent = new Intent();
        intent.setClass(this, UpdateActivity.class);
        startActivity(intent);
    }

    private void startToBindLocationMgrActivity() {
        Intent intent = new Intent();
        intent.setClass(this, BindLocationMgrActivity.class);
        startActivity(intent);
    }

    private void startToSelectChildActivity() {
        Intent intent = new Intent();
        intent.setClass(this, ChildListActivity.class);
        startActivity(intent);
    }

}
