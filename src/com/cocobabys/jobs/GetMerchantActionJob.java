package com.cocobabys.jobs;

import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.ActionMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

public class GetMerchantActionJob extends MyJob{
    private Handler handler;
    private int     merchantID;

    public GetMerchantActionJob(Handler handler, int merchantID){
        this.handler = handler;
        this.merchantID = merchantID;
    }

    @Override
    public void run(){
        MethodResult bret = new MethodResult(EventType.ACTION_GET_FAIL);
        MyProxy proxy = new MyProxy();
        MyProxyImpl bind = (MyProxyImpl)proxy.bind(new MyProxyImpl(){
            @Override
            public MethodResult handle() throws Exception{
                MethodResult result = ActionMethod.getMethod().getInfoBelongToMerchant(merchantID);
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
            msg.obj = bret.getResultObj();
            msg.what = bret.getResultType();
            handler.sendMessage(msg);
        }
    }

}
