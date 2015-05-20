package com.cocobabys.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import de.greenrobot.event.EventBus;

public class BaseEventFragmentActivity extends FragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Register
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Unregister
		EventBus.getDefault().unregister(this);
	}
}
