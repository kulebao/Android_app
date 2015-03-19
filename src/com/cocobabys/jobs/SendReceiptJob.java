package com.cocobabys.jobs;

import android.os.Handler;
import android.os.Message;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.MethodResult;
import com.cocobabys.net.NewsMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

public class SendReceiptJob extends MyJob{
    private Handler handler;
    private int     newsID;

    public SendReceiptJob(Handler handler, int newsID){
        this.handler = handler;
        this.newsID = newsID;
    }

    @Override
    public void run(){
        MethodResult bret = new MethodResult(EventType.POST_RECEIPT_FAIL);
        try{
            MyProxy proxy = new MyProxy();
            MyProxyImpl bind = (MyProxyImpl)proxy.bind(new MyProxyImpl(){
                @Override
                public MethodResult handle() throws Exception{
                    MethodResult result = NewsMethod.getMethod().sendFeedBack(newsID);
                    return result;
                }
            });

            bret = MethodUtils.getBindResult(bind);

        } catch(Exception e){
            e.printStackTrace();
        }
        finally{
            Message msg = Message.obtain();
            msg.what = bret.getResultType();
            msg.arg1 = newsID;
            handler.sendMessage(msg);
        }

    }

}
