package com.cocobabys.im;

import com.cocobabys.R;
import com.cocobabys.activities.ActivityHelper;
import com.cocobabys.activities.MyApplication;
import com.cocobabys.utils.IMUtils;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

/**
 * Created by Bob on 15/8/18. 会话列表
 */
@SuppressLint("NewApi")
public class ConversationListActivity extends FragmentActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.im_conversationlist);

        setActionBarTitle("会话列表");

        isReconnect();
    }

    /**
     * 设置 actionbar title
     */
    private void setActionBarTitle(String title){
        ActivityHelper.setTitle(this, title);
    }

    /**
     * 加载 会话列表 ConversationListFragment
     */
    private void enterFragment(){

        ConversationListFragment fragment = (ConversationListFragment)getSupportFragmentManager()
                .findFragmentById(R.id.conversationlist);

        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon().appendPath("conversationlist")
                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") // 设置私聊会话非聚合显示
                .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")// 设置群组会话聚合显示
                .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "false")// 设置讨论组会话非聚合显示
                .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "false")// 设置系统会话非聚合显示
                .build();

        fragment.setUri(uri);
    }

    /**
     * 判断消息是否是 push 消息
     *
     */
    private void isReconnect(){

        Intent intent = getIntent();

        String token = IMUtils.getToken();

        // push或通知过来
        if(intent != null && intent.getData() != null && intent.getData().getScheme().equals("rong")){

            // 通过intent.getData().getQueryParameter("push") 为true，判断是否是push消息
            if(intent.getData().getQueryParameter("push") != null
                    && intent.getData().getQueryParameter("push").equals("true")){
                reconnect(token);
            } else{
                // 程序切到后台，收到消息后点击进入,会执行这里
                if(RongIM.getInstance() == null || RongIM.getInstance().getRongIMClient() == null){
                    reconnect(token);
                } else{
                    enterFragment();
                }
            }
        }

    }

    /**
     * 重连
     *
     * @param token
     */
    private void reconnect(String token){

        if(getApplicationInfo().packageName.equals(MyApplication.getCurProcessName(getApplicationContext()))){

            RongIM.connect(token, new RongIMClient.ConnectCallback(){
                @Override
                public void onTokenIncorrect(){

                }

                @Override
                public void onSuccess(String s){
                    enterFragment();
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode){

                }
            });
        }
    }
}
