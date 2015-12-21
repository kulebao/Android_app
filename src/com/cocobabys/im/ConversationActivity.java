package com.cocobabys.im;

import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.fragment.MessageListFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Conversation.ConversationType;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.cocobabys.R;
import com.cocobabys.activities.ActivityHelper;
import com.cocobabys.activities.GroupSettingActivity;
import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.IMGroupInfo;
import com.cocobabys.event.EmptyEvent;
import com.cocobabys.utils.IMUtils;
import com.cocobabys.utils.Utils;

import de.greenrobot.event.EventBus;

/**
 * Created by Bob on 15/8/18. 会话页面
 */
public class ConversationActivity extends FragmentActivity{

    private String                        mTargetId;

    /**
     * 刚刚创建完讨论组后获得讨论组的id 为targetIds，需要根据 为targetIds 获取 targetId
     */
    private String                        mTargetIds;

    /**
     * 会话类型
     */
    private Conversation.ConversationType mConversationType;
    private String                        title;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        getIntentDate(intent);

        if(mConversationType.equals(ConversationType.SYSTEM)){
            setContentView(R.layout.im_messagelist);
        } else{
            setContentView(R.layout.im_conversation);
        }

        initUI();

        isReconnect(intent);

        // 关闭activity堆栈里的已经创建的ConversationActivity，ConversationActivity只保持一个存在
        EventBus.getDefault().post(new EmptyEvent());
    }

    @Override
    protected void onPause(){
        super.onPause();
        closeKeyBoard();
        boolean registered = EventBus.getDefault().isRegistered(this);
        Log.d("", "ZZZZZ onPause register registered=" + registered);
        if(!registered){
            EventBus.getDefault().register(this);
        }
    }

    public void closeKeyBoard(){
        View view = getWindow().peekDecorView();
        if(view != null){
            InputMethodManager inputmanger = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void onEvent(EmptyEvent emptyEvent){
        Log.d("", "ZZZZZ  receive emptyEvent");
        finish();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        boolean registered = EventBus.getDefault().isRegistered(this);
        Log.d("", "ZZZZZ onDestroy unregister registered=" + registered);
        if(registered){
            EventBus.getDefault().unregister(this);
        }
    }

    /**
     * 展示如何从 Intent 中得到 融云会话页面传递的 Uri
     */
    private void getIntentDate(Intent intent){

        mTargetId = intent.getData().getQueryParameter("targetId");
        mTargetIds = intent.getData().getQueryParameter("targetIds");
        title = intent.getData().getQueryParameter("title");
        // intent.getData().getLastPathSegment();//获得当前会话类型
        mConversationType = Conversation.ConversationType.valueOf(intent.getData().getLastPathSegment()
                .toUpperCase(Locale.getDefault()));
    }

    private void initUI(){
        enterFragment(mConversationType, mTargetId);

        if(Conversation.ConversationType.GROUP.equals(mConversationType)){
            Log.d("", "GROUP ");
            // 刷新群组信息
            // IMHelper.updateGroupInfoCache(mTargetId);

            ImageView groupMember = (ImageView)findViewById(R.id.rightImage);
            groupMember.setVisibility(View.VISIBLE);
            groupMember.setImageResource(R.drawable.contactbtn);

            groupMember.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    IMGroupInfo group = DataMgr.getInstance().getIMGroupInfoByGroupID(mTargetId);
                    if(group == null){
                        Utils.makeToast(ConversationActivity.this, "获取群组信息失败！");
                        return;
                    }
                    Log.d("", "group =" + group.toString());
                    Intent intent = new Intent(ConversationActivity.this, GroupSettingActivity.class);
                    intent.putExtra(ConstantValue.CLASS_ID, group.getClass_id() + "");
                    intent.putExtra(ConstantValue.IM_GROUP_ID, mTargetId);
                    intent.putExtra(ConstantValue.IM_GROUP_NAME, title);
                    startActivity(intent);
                }
            });
        } else{
            Log.d("", "Private ");
        }

        setActionBarTitle(title);
    }

    /**
     * 加载会话页面 ConversationFragment
     *
     * @param mConversationType
     * @param mTargetId
     */
    @SuppressLint("NewApi")
    private void enterFragment(Conversation.ConversationType mConversationType, String mTargetId){

        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon().appendPath("conversation")
                .appendPath(mConversationType.getName().toLowerCase()).appendQueryParameter("targetId", mTargetId)
                .build();

        if(mConversationType.equals(ConversationType.SYSTEM)){
            MessageListFragment fragment = (MessageListFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.messagelist);
            fragment.setUri(uri);
        } else{
            ConversationFragment fragment = (ConversationFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.conversation);
            fragment.setUri(uri);
        }

    }

    /**
     * 判断消息是否是 push 消息
     */
    private void isReconnect(Intent intent){

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
                    // setProvider();
                    enterFragment(mConversationType, mTargetId);
                }
            }
        }
    }

    // private void setProvider() {
    // Log.d("", "setProvider");
    // IMHelper imHelper = new IMHelper();
    // RongIM.setUserInfoProvider(imHelper, true);// 设置用户信息提供者。
    // RongIM.setGroupInfoProvider(imHelper, true);// 设置群组信息提供者。
    // }

    /**
     * 设置 actionbar title
     */
    private void setActionBarTitle(String title){
        ActivityHelper.setTitle(this, title);
    }

    /**
     * 重连
     *
     * @param token
     */
    private void reconnect(final String token){

        if(getApplicationInfo().packageName.equals(MyApplication.getCurProcessName(getApplicationContext()))){

            RongIM.connect(token, new RongIMClient.ConnectCallback(){
                @Override
                public void onTokenIncorrect(){
                    Log.e("", "reconnect token invalid :" + token);
                }

                @Override
                public void onSuccess(String s){
                    // setProvider();
                    enterFragment(mConversationType, mTargetId);
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode){
                    Log.e("", "reconnect error :" + errorCode);
                }
            });
        }
    }
}
