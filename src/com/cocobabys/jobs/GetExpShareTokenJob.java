package com.cocobabys.jobs;

import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.info.ExpInfo;
import com.cocobabys.net.ExpMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

public class GetExpShareTokenJob extends MyJob{

    private ExpInfo info;
    private Handler handler;

    public GetExpShareTokenJob(ExpInfo info, Handler handler){
        this.info = info;
        this.handler = handler;
    }

    @Override
    public void run(){
        MethodResult bret = new MethodResult(EventType.GET_EXP_TOKEN_FAIL);

        MyProxy proxy = new MyProxy();
        MyProxyImpl bind = (MyProxyImpl)proxy.bind(new MyProxyImpl(){

            @Override
            public MethodResult handle() throws Exception{
                MethodResult result = ExpMethod.getMethod().getShareToken(info);
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
