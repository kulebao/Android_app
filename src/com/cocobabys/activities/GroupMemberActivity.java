package com.cocobabys.activities;

import com.cocobabys.R;
import com.cocobabys.constant.ConstantValue;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class GroupMemberActivity extends UmengStatisticsActivity{

    private String classid;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_member);
        classid = getIntent().getStringExtra(ConstantValue.CLASS_ID);
    }

    public void teacher(View view){
        Intent intent = new Intent(GroupMemberActivity.this, TeacherGroupActivity.class);
        intent.putExtra(ConstantValue.CLASS_ID, classid);
        startActivity(intent);
    }

    public void parent(View view){
        Intent intent = new Intent(GroupMemberActivity.this, ParentGroupActivity.class);
        intent.putExtra(ConstantValue.CLASS_ID, classid);
        startActivity(intent);
    }

}
