package com.cocobabys.jobs;

import com.cocobabys.bean.PullToRefreshListInfo;
import com.cocobabys.constant.EventType;
import com.cocobabys.net.MerchantMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

import android.os.Handler;
import android.os.Message;

public class GetMechantJob extends MyJob{
    private Handler               handler;
    private PullToRefreshListInfo info;
    private int                   category;
    private static final int      LIMIT_TIME = 300;

    public GetMechantJob(Handler handler, PullToRefreshListInfo info, int category){
        this.handler = handler;
        this.info = info;
        this.category = category;
    }

    @Override
    public void run(){
        long current = System.currentTimeMillis();

        MethodResult bret = new MethodResult(EventType.MECHANT_GET_FAIL);
        MyProxy proxy = new MyProxy();
        MyProxyImpl bind = (MyProxyImpl)proxy.bind(new MyProxyImpl(){
            @Override
            public MethodResult handle() throws Exception{
                MethodResult result = MerchantMethod.getMethod().getInfo(info, category);
                return result;
            }
        });

        try{

            long now = System.currentTimeMillis();
            bret = MethodUtils.getBindResult(bind);
            long ellapse = now - current;
            if(ellapse < LIMIT_TIME){
                try{
                    Thread.sleep(LIMIT_TIME - ellapse);
                } catch(InterruptedException e){
                    e.printStackTrace();
                }
            }

        } catch(Exception e){
            e.printStackTrace();
        }
        finally{
            Message msg = Message.obtain();
            msg.arg1 = info.getType();
            msg.what = bret.getResultType();
            msg.obj = bret.getResultObj();
            handler.sendMessage(msg);
        }
    }

}
