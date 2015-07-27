package com.cocobabys.listener;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public abstract class MyPullToRefreshOnItemClickListener implements
		OnItemClickListener {

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (id == -1) {
			// 点击的是headerView或者footerView
			return;
		}
		int realPosition = (int) id;

		handleClick(realPosition);
	}

	public abstract void handleClick(int realPosition);
}
