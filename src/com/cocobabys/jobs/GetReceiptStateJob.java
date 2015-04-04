package com.cocobabys.jobs;

import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.MethodResult;
import com.cocobabys.net.ReceiptMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

public class GetReceiptStateJob extends MyJob{
    private Handler handler;
    private int     newsServerID;

    public GetReceiptStateJob(Handler handler, int newsServerID){
        this.handler = handler;
        this.newsServerID = newsServerID;
    }

    @Override
    public void run(){
        MethodResult bret = new MethodResult(EventType.GET_RECEIPT_FAIL);

        MyProxy proxy = new MyProxy();
        MyProxyImpl bind = (MyProxyImpl)proxy.bind(new MyProxyImpl(){
            @Override
            public MethodResult handle() throws Exception{
                MethodResult result = ReceiptMethod.getMethod().getReceiptState(newsServerID);
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
            msg.obj = bret.getResultObj();
            handler.sendMessage(msg);
        }
    }
}
