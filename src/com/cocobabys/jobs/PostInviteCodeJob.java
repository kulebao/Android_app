package com.cocobabys.jobs;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.InvitationMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

import android.os.Handler;
import android.os.Message;

public class PostInviteCodeJob extends MyJob{
    private Handler handler;
    private String  inviteePhone;

    public PostInviteCodeJob(Handler handler, String inviteePhone){
        this.handler = handler;
        this.inviteePhone = inviteePhone;
    }

    @Override
    public void run(){
        MethodResult bret = new MethodResult(EventType.GET_AUTH_CODE_FAIL);

        MyProxy proxy = new MyProxy();
        MyProxyImpl bind = (MyProxyImpl)proxy.bind(new MyProxyImpl(){
            @Override
            public MethodResult handle() throws Exception{
                MethodResult result = InvitationMethod.getMethod().getVerifyCode(inviteePhone);
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
