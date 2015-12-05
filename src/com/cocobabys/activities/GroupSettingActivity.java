package com.cocobabys.activities;

import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.model.Conversation.ConversationType;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.customview.CheckSwitchButton;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.im.IMHelper;
import com.cocobabys.utils.IMUtils;
import com.cocobabys.utils.Utils;

public class GroupSettingActivity extends UmengStatisticsActivity{

    private String            classid;
    private String            groupID;
    private CheckSwitchButton disturbButton;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_setting);
        groupName = getIntent().getStringExtra(ConstantValue.IM_GROUP_NAME);
        groupID = getIntent().getStringExtra(ConstantValue.IM_GROUP_ID);
        classid = getIntent().getStringExtra(ConstantValue.CLASS_ID);
        
        initUI();
    }

    public void groupMember(View view){
        Intent intent = new Intent(GroupSettingActivity.this, ContactListActivity.class);
        intent.putExtra(ConstantValue.CLASS_ID, classid);
        startActivity(intent);
    }

    public void clearChatRecord(View view){
        Utils.showTwoBtnResDlg(R.string.clear_group_chat, this, new OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                clearImpl();
            }
        });
    }

    private void clearImpl(){
        IMHelper.clearChatRecord(ConversationType.GROUP, groupID, new ResultCallback<Boolean>(){
            @Override
            public void onSuccess(Boolean arg0){
                Utils.makeToast(GroupSettingActivity.this, R.string.clear_success);
            }

            @Override
            public void onError(ErrorCode arg0){
                Utils.makeToast(GroupSettingActivity.this, "清除失败 error =" + arg0);
            }
        });
    }

    private void initUI(){
        TextView class_name = (TextView)findViewById(R.id.class_name);
        class_name.setText(groupName);
        
        TextView schoolname = (TextView)findViewById(R.id.schoolname);
        schoolname.setText(DataMgr.getInstance().getSchoolInfo().getSchool_name());
        
        disturbButton = (CheckSwitchButton)findViewById(R.id.checkSwithcButton);

        disturbButton = (CheckSwitchButton)findViewById(R.id.checkSwithcButton);

        disturbButton.setChecked(IMUtils.isMessageDisturbEnable(groupID));
        disturbButton.setOnCheckedChangeListener(new OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked){
                Log.d("", "SetConversationNotificationFragment  isChecked = " + isChecked);
                IMUtils.setMessageDisturbEnable(groupID, isChecked);

            }
        });
    }

}
