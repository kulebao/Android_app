package com.cocobabys.activities;

import com.cocobabys.R;
import com.cocobabys.fragment.ActionFragment;
import com.cocobabys.fragment.ShopFragment;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class BusinessActivity extends FragmentActivity {
	private static final int ACTION = 0;
	private static final int MERCHANT = 1;

	private int currentIndex = ACTION;
	private ImageView actionView;
	private ImageView shopView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.business);

		initView();
		Log.d("", "MainActivity action");
		moveToAction();
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this); // 统计时长
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void initView() {
		actionView = (ImageView) findViewById(R.id.actionView);
		shopView = (ImageView) findViewById(R.id.shopView);
	}

	private void moveToAction() {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		ActionFragment fragment = new ActionFragment();
		transaction.replace(R.id.djc_fragment, fragment);
		transaction.commit();
	}

	private void moveToShop() {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		ShopFragment fragment = new ShopFragment();
		transaction.replace(R.id.djc_fragment, fragment);
		transaction.commit();
	}

	public void action(View view) {
		Log.d("", "action currentIndex=" + currentIndex);
		if (currentIndex == ACTION) {
			return;
		}

		actionView.setImageResource(R.drawable.action1);
		shopView.setImageResource(R.drawable.shop0);

		moveToAction();
		currentIndex = ACTION;
	}

	public void shop(View view) {
		Log.d("", "shop currentIndex=" + currentIndex);

		if (currentIndex == MERCHANT) {
			return;
		}

		actionView.setImageResource(R.drawable.action0);
		shopView.setImageResource(R.drawable.shop1);

		moveToShop();
		currentIndex = MERCHANT;
	}
}
