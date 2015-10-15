package com.cocobabys.jobs;

import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.CardMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

public class BindCardJob extends MyJob{
    private Handler handler;
    private String  cardNum;

    public BindCardJob(Handler handler, String cardNum){
        this.handler = handler;
        this.cardNum = cardNum;
    }

    @Override
    public void run(){
        MethodResult bret = new MethodResult(EventType.BIND_CARD_FAIL);
        MyProxy proxy = new MyProxy();
        MyProxyImpl bind = (MyProxyImpl)proxy.bind(new MyProxyImpl(){

            @Override
            public MethodResult handle() throws Exception{
                MethodResult result = CardMethod.getMethod().bindCard(cardNum);
                return result;
            }
        });

        try{
            bret = MethodUtils.getBindResult(bind);
        } catch(Exception e){
            e.printStackTrace();
        }
        finally{
            Message msg = Message.obtain();
            msg.what = bret.getResultType();
            handler.sendMessage(msg);
        }
    }

}
