package com.cocobabys.customview;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.Button;

import com.cocobabys.R;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.taskmgr.AuthCodeCountDownTask;

public class CountDownButton extends Button{

    private AsyncTask<Void, Void, Void> authCodeCountDownTask;
    private Handler                     handler;
    private Context                     context;
    private int                         countMax = ConstantValue.TIME_LIMIT_TO_GET_AUTHCODE_AGAIN;

    public CountDownButton(Context context){
        super(context);
        this.context = context;
        init();
    }

    public CountDownButton(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    public CountDownButton(Context context, AttributeSet attrs){
        super(context, attrs);
        this.context = context;
        init();
    }

    public void setCountMax(int count){
        countMax = count;
    }

    private void init(){
        handler = new Handler(new Callback(){

            @Override
            public boolean handleMessage(Message msg){
                switch(msg.what){
                    case EventType.AUTHCODE_COUNTDOWN_GO:
                        handleCountDownGo(msg.arg1);
                        break;
                    case EventType.AUTHCODE_COUNTDOWN_OVER:
                        handleCountDownOver();
                        break;
                    default:
                        break;
                }
                // 返回true 终止消息继续传递
                return true;
            }
        });
    }

    public void countdown(){
        if(isEnabled()){
            disableGetAuthBtn();
            runAuthCodeCountDownTask();
        }
    }

    public void cancel(){
        if(authCodeCountDownTask != null){
            authCodeCountDownTask.cancel(true);
        }
    }

    public void disableGetAuthBtn(){
        setTextColor(context.getResources().getColor(R.color.dark_gray));
        setBackgroundResource(R.drawable.small_btn);
        setEnabled(false);
    }

    public void enableGetAuthBtn(){
        setEnabled(true);
        setBackgroundResource(R.drawable.small_btn);
        setTextColor(context.getResources().getColor(R.color.white));
    }

    private void runAuthCodeCountDownTask(){
        authCodeCountDownTask = new AuthCodeCountDownTask(handler, countMax).execute();
    }

    private void handleCountDownOver(){
        setText(context.getResources().getString(R.string.getAuthCode));
        enableGetAuthBtn();
    }

    private void handleCountDownGo(int second){
        setText(String.format(context.getResources().getString(R.string.getAuthCodeCountDown), String.valueOf(second)));
    }
}
