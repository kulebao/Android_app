package com.cocobabys.activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.utils.DataUtils;

public class TransitActivity extends UmengStatisticsActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transit);
		initView();
	}

	private void initView() {
		TextView welcomeView = (TextView) findViewById(R.id.welcomeView);
		String firstlogin = String.format(
				getResources().getString(R.string.firstwelcome),
				DataUtils.getProp(JSONConstant.USERNAME),DataUtils.getProp(JSONConstant.ACCOUNT_NAME));
		
		welcomeView.setText(firstlogin);
		
		Button startMainView = (Button) findViewById(R.id.startMainView);
		startMainView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startToMainActivity();
			}
		});
	}
	
	private void startToMainActivity() {
	}
}
