package com.cocobabys.activities;

import com.cocobabys.R;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.customview.CheckSwitchButton;
import com.cocobabys.utils.IMUtils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class GroupMemberActivity extends UmengStatisticsActivity {

	private String classid;
	private String groupID;
	private CheckSwitchButton disturbButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group_member);
		groupID = getIntent().getStringExtra(ConstantValue.IM_GROUP_ID);
		classid = getIntent().getStringExtra(ConstantValue.CLASS_ID);

		initUI();
	}

	public void teacher(View view) {
		Intent intent = new Intent(GroupMemberActivity.this, TeacherGroupActivity.class);
		intent.putExtra(ConstantValue.CLASS_ID, classid);
		startActivity(intent);
	}

	public void parent(View view) {
		Intent intent = new Intent(GroupMemberActivity.this, ParentGroupActivity.class);
		intent.putExtra(ConstantValue.CLASS_ID, classid);
		startActivity(intent);
	}

	private void initUI() {
		disturbButton = (CheckSwitchButton) findViewById(R.id.checkSwithcButton);

		disturbButton = (CheckSwitchButton) findViewById(R.id.checkSwithcButton);

		disturbButton.setChecked(IMUtils.isMessageDisturbEnable(groupID));
		disturbButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
				Log.d("", "SetConversationNotificationFragment  isChecked = " + isChecked);
				IMUtils.setMessageDisturbEnable(groupID, isChecked);

			}
		});
	}

}
