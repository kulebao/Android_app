package com.cocobabys.activities;

import com.umeng.analytics.MobclickAgent;

import android.app.Activity;

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
