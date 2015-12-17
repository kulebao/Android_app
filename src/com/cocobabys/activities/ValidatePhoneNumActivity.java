package com.cocobabys.activities;

import com.cocobabys.R;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.taskmgr.ValidatePhoneNumTask;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ValidatePhoneNumActivity extends MyActivity{
    private Button         sendPhoneNumBtn;
    private ProgressDialog dialog;
    private Handler        handler;
    private EditText       inuputnumView;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkphone);
        initDialog();
        initView();
        initHandler();
    }

    private void initHandler(){
        handler = new MyHandler(this, dialog){
            @Override
            public void handleMessage(Message msg){
                if(ValidatePhoneNumActivity.this.isFinishing()){
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                super.handleMessage(msg);
                Log.w("ValidatePhoneNumActivity", "ValidatePhoneNumActivity msg.what=" + msg.what);
                switch(msg.what){
                    case EventType.PHONE_NUM_IS_INVALID:
                        Utils.showSingleBtnEventDlg(EventType.PHONE_NUM_IS_INVALID, ValidatePhoneNumActivity.this);
                        break;
                    case EventType.PHONE_NUM_IS_FIRST_USE:
                        startAuthCodeActivity();
                        break;
                    case EventType.PHONE_NUM_IS_ALREADY_BIND:
                        handleAlreadyBind();
                        break;
                    default:
                        break;
                }
            }

        };
    }

    private void handleAlreadyBind(){
        String phone = inuputnumView.getText().toString();
        if(TextUtils.isEmpty(DataUtils.getUndeleteableProp(phone))){
            // 该号码之前没有在此机器上登录过，进入获取短信验证码流程
            startAuthCodeActivity();
        } else{
            // 该号码已经在此机器上登录过，直接进入登录流程
            startLoginActivity();
        }
    }

    private void startLoginActivity(){
        // 防止启动到下一个界面前，btn又被点击
        sendPhoneNumBtn.setEnabled(false);
        Intent intent = createIntent();
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void startAuthCodeActivity(){
        // 防止启动到下一个界面前，btn又被点击
        sendPhoneNumBtn.setEnabled(false);
        Intent intent = createIntent();
        intent.setClass(this, ValidateAuthCodeActivity.class);
        startActivity(intent);
        finish();
    }

    private Intent createIntent(){
        Bundle bundle = new Bundle();
        bundle.putString(JSONConstant.PHONE_NUM, inuputnumView.getText().toString());

        Intent intent = new Intent();
        intent.putExtras(bundle);
        return intent;
    }

    private void initView(){
        inuputnumView = (EditText)findViewById(R.id.inuputnumView);
        if(MyApplication.getInstance().isForTest()){
            inuputnumView.setText("13408654680");
        }

        sendPhoneNumBtn = (Button)findViewById(R.id.sendPhoneNumBtn);
        sendPhoneNumBtn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(Utils.checkPhoneNum(getPhoneNum())){
                    try{
                        Utils.closeKeyBoard(ValidatePhoneNumActivity.this);
                        runValidatePhoneNumTask();
                        dialog.show();
                    } catch(Exception e){
                        dialog.cancel();
                        e.printStackTrace();
                    }
                } else{
                    Utils.showSingleBtnEventDlg(EventType.PHONE_NUM_INPUT_ERROR, ValidatePhoneNumActivity.this);
                }
            }
        });
    }

    public void initDialog(){
        dialog = new ProgressDialog(this);
        dialog.setMessage(getResources().getString(R.string.validate_phone_num));
        dialog.setCancelable(false);
    }

    public String getPhoneNum(){
        String phonenum = inuputnumView.getText().toString();
        return phonenum;
    }

    private void runValidatePhoneNumTask(){
        new ValidatePhoneNumTask(handler, getPhoneNum()).execute();
        // ValidatePhoneNumJob validatePhoneNumJob = new ValidatePhoneNumJob(
        // handler, getPhoneNum());
        // MyThreadPoolMgr.getGenericService().submit(validatePhoneNumJob);
    }
}