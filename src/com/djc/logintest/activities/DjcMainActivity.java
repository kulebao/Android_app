package com.djc.logintest.activities;

import org.json.JSONObject;

import android.app.ProgressDialog;
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
import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.constant.ServerUrls;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.handler.MyHandler;
import com.djc.logintest.net.HttpResult;
import com.djc.logintest.net.HttpsMethod;
import com.djc.logintest.push.PushModel;
import com.djc.logintest.taskmgr.CheckUpdateTask;
import com.djc.logintest.utils.Utils;

public class DjcMainActivity extends MyActivity {
    private Handler handler;
    private ProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initView();
        initDialog();
        initHandler();
        bindPush();
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
                if (DjcMainActivity.this.isFinishing()) {
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                super.handleMessage(msg);
                switch (msg.what) {
                case EventType.HAS_NEW_VERSION:
                    startToUpdateActivity();
                    break;
                case EventType.HAS_NO_VERSION:
                    Toast.makeText(DjcMainActivity.this, R.string.no_new_version, Toast.LENGTH_SHORT)
                            .show();
                    break;
                default:
                    break;
                }
            }
        };
    }

    public void bindPush() {
        PushModel pushModel = PushModel.getPushModel();
        // 如果没有绑定，则进行绑定
        if (!pushModel.isBinded()) {
            pushModel.bind();
        }

        // 如果没有设置默认tag，则设置tag
        if (!pushModel.getTags().contains(DataMgr.getInstance().getSchoolID())) {
            pushModel.setAllDefaultTag();
        }
    }

    private void initView() {
        Button settingBtn = (Button) findViewById(R.id.setting);
        settingBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startToSettingActivity();
            }
        });

        Button checkVersionBtn = (Button) findViewById(R.id.checkVersion);
        checkVersionBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                runCheckUpdateTask();
            }
        });

        Button location = (Button) findViewById(R.id.location);
        location.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        testLocation();
                    }
                }).start();

            }
        });
    }

    protected void testLocation() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(JSONConstant.ACCOUNT_NAME, Utils.getAccount());
            jsonObject.put(JSONConstant.LATITUDE, "30.650387");
            jsonObject.put(JSONConstant.LONGITUDE, "104.040387");
            String command = jsonObject.toString();
            Log.d("DDD TEST", "str ="+command);
            HttpResult result = HttpsMethod.sendPostCommand(ServerUrls.LOCATION, command);
            // if (result.getResCode() == HttpStatus.SC_OK) {
            // Toast.makeText(this, "定位成功", Toast.LENGTH_SHORT).show();
            // } else {
            // Toast.makeText(this, "定位失败  errorCode:" + result.getResCode(),
            // Toast.LENGTH_SHORT)
            // .show();
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void runCheckUpdateTask() {
        dialog.show();
        new CheckUpdateTask(handler, Utils.getAccount(), Utils.getVersionCode()).execute();
    }

    private void startToUpdateActivity() {
        Intent intent = new Intent();
        intent.setClass(this, UpdateActivity.class);
        startActivity(intent);
    }

    private void startToSettingActivity() {
        Intent intent = new Intent();
        intent.setClass(this, SettingActivity.class);
        startActivity(intent);
    }

    private void startToLoadingActivity() {
        Intent intent = new Intent();
        intent.setClass(this, LoadingActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.isLoginout()) {
            startToLoadingActivity();
        }
    }

}
