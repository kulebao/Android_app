package com.cocobabys.activities;

import android.app.Activity;

import com.umeng.analytics.MobclickAgent;

public class UmengStatisticsActivity extends Activity {
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
