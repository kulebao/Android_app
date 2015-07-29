package com.cocobabys.jobs;

import android.os.Handler;
import android.os.Message;

import com.cocobabys.bean.PullToRefreshListInfo;
import com.cocobabys.constant.EventType;
import com.cocobabys.net.ActionMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

public class GetActionJob extends MyJob{
    private Handler               handler;
    private PullToRefreshListInfo info;
    private static final int      LIMIT_TIME = 300;

    public GetActionJob(Handler handler, PullToRefreshListInfo info){
        this.handler = handler;
        this.info = info;
    }

    @Override
    public void run(){
        long current = System.currentTimeMillis();

        MethodResult bret = new MethodResult(EventType.ACTION_GET_FAIL);
        MyProxy proxy = new MyProxy();
        MyProxyImpl bind = (MyProxyImpl)proxy.bind(new MyProxyImpl(){
            @Override
            public MethodResult handle() throws Exception{
                MethodResult result = ActionMethod.getMethod().getInfo(info);
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
