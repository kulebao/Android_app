package com.cocobabys.jobs;

import java.util.List;

import rx.Observable;
import rx.Observer;
import android.os.Handler;
import android.util.Log;

import com.cocobabys.constant.EventType;
import com.cocobabys.net.IMMethod;
import com.cocobabys.net.MethodResult;
import com.cocobabys.threadpool.MyJob;

public class JoinGroupJob extends MyJob{

    private Handler      handler;
    private List<String> classidList;
    private int          successCount = 0;

    public JoinGroupJob(Handler handler, List<String> classidList){
        this.handler = handler;
        this.classidList = classidList;
    }

    @Override
    public void run(){
        Observable.from(classidList).doOnEach(new Observer<String>(){

            @Override
            public void onCompleted(){
                int event = successCount > 0 ? EventType.JOIN_IM_GROUP_SUCCESS : EventType.JOIN_IM_GROUP_FAIL;
                handler.sendEmptyMessage(event);
            }

            @Override
            public void onError(Throwable arg0){
                int event = successCount > 0 ? EventType.JOIN_IM_GROUP_SUCCESS : EventType.JOIN_IM_GROUP_FAIL;
                handler.sendEmptyMessage(event);
            }

            @Override
            public void onNext(String classid){
                try{
                    Log.d("", "joinGroupInfo classid=" + classid);
                    MethodResult result = IMMethod.getMethod().joinGroupInfo(classid);
                    if(result.getResultType() == EventType.JOIN_IM_GROUP_SUCCESS){
                        successCount++;
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).subscribe();

    }

}
