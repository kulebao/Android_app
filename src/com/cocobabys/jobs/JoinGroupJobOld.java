package com.cocobabys.jobs;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.OperationCallback;
import android.os.Handler;
import android.util.Log;

import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.info.IMGroupInfo;
import com.cocobabys.net.IMMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;
import com.cocobabys.threadpool.MyJob;
import com.cocobabys.utils.MethodUtils;

public class JoinGroupJobOld extends MyJob{

    private Handler handler;
    private String  classid;

    public JoinGroupJobOld(Handler handler, String classid){
        this.handler = handler;
        this.classid = classid;
    }

    @Override
    public void run(){
        MethodResult bret = new MethodResult(EventType.GET_IM_GROUP_FAIL);
        try{
            MyProxy proxy = new MyProxy();
            MyProxyImpl bind = (MyProxyImpl)proxy.bind(new MyProxyImpl(){
                @Override
                public MethodResult handle() throws Exception{
                    MethodResult result = IMMethod.getMethod().getGroupInfo(classid);
                    return result;
                }
            });

            bret = MethodUtils.getBindResult(bind);

            if(bret.getResultType() == EventType.GET_IM_GROUP_SUCCESS){
                IMGroupInfo groupInfo = (IMGroupInfo)bret.getResultObj();

                RongIM.getInstance().getRongIMClient()
                        .joinGroup(groupInfo.getGroup_id(), groupInfo.getGroup_name(), new OperationCallback(){

                            @Override
                            public void onSuccess(){
                                Log.d("", "JOIN_IM_GROUP_SUCCESS");
                                handler.sendEmptyMessage(EventType.JOIN_IM_GROUP_SUCCESS);
                            }

                            @Override
                            public void onError(ErrorCode errorCode){
                                Log.d("",
                                      "JOIN_IM_GROUP_FAIL err :" + errorCode.getMessage() + " code="
                                              + errorCode.getValue());
                                handler.sendEmptyMessage(EventType.JOIN_IM_GROUP_FAIL);
                            }
                        });
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

}
