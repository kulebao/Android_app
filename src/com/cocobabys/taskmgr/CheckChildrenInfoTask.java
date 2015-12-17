package com.cocobabys.taskmgr;

import java.util.List;

import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.IMGroupInfo;
import com.cocobabys.im.IMHelper;
import com.cocobabys.net.ChildMethod;
import com.cocobabys.proxy.MyProxy;
import com.cocobabys.proxy.MyProxyImpl;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CheckChildrenInfoTask extends AsyncTask<Void, Void, Integer>{

    private Handler handler;

    public CheckChildrenInfoTask(Handler handler){
        this.handler = handler;
    }

    @Override
    protected Integer doInBackground(Void... params){
        MyProxy proxy = new MyProxy();
        MyProxyImpl bind = (MyProxyImpl)proxy.bind(new MyProxyImpl(){
            @Override
            public Object handle() throws Exception{
                // int result = ChildMethod.getMethod()
                // .getChildrenInfo();
                int result = ChildMethod.getMethod().getRelationship();
                return result;
            }
        });

        Integer result = EventType.NET_WORK_INVALID;
        try{
            result = (Integer)bind.handle();

            updateIMGroupInfo();

        } catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    // 如果班级比群组少，那么退出班级相关群组
    private void updateIMGroupInfo(){
        if(MyApplication.getInstance().isForTest()){
            List<String> allClassID = DataMgr.getInstance().getAllClassID();
            List<IMGroupInfo> allIMGroupInfo = DataMgr.getInstance().getAllIMGroupInfo();

            for(final IMGroupInfo groupInfo : allIMGroupInfo){
                if(!allClassID.contains(groupInfo.getClass_id())){
                    Log.d("", "quit group info=" + groupInfo.toString());
                    IMHelper.quitGroup(groupInfo);
                }
            }
        }
    }

    @Override
    protected void onPostExecute(Integer result){
        super.onPostExecute(result);
        sendCheckChildInfoMsg(result);
        sendCheckNewDataMsg();
    }

    private void sendCheckNewDataMsg(){
        // 此时更新后的小孩数据已经写入数据库，在这里通知主界面检查全部数据是否有更新
        // 并提示用户
        Message msg = Message.obtain();
        msg.what = EventType.CHECK_NEW_DATA;
        handler.sendMessage(msg);
    }

    private void sendCheckChildInfoMsg(Integer result){
        Message msg = Message.obtain();
        msg.what = result;
        handler.sendMessage(msg);
    }
}
