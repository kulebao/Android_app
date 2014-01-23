package com.djc.logintest.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.djc.logintest.R;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.utils.Utils;

public class TransitActivity extends Activity {
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
				Utils.getProp(JSONConstant.USERNAME),Utils.getProp(JSONConstant.ACCOUNT_NAME));
		
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
