package com.djc.logintest.activities;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.djc.logintest.R;
import com.djc.logintest.adapter.SwipeListAdapter;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.SwipeInfo;

public class SwipeListActivity extends UmengStatisticsActivity {
	private ListView list;
	private SwipeListAdapter adapter;
	private String date;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notice_list);
		getParam();
		ActivityHelper.setBackKeyLitsenerOnTopbar(this,
				R.string.swap);
		initListAdapter();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	private void getParam() {
		date = getIntent().getStringExtra(ConstantValue.SWIPE_DATE);
	}

	private void initListAdapter() {
		List<SwipeInfo> listinfo = DataMgr.getInstance().getAllSwipeCardNotice(
				date);
		adapter = new SwipeListAdapter(this, listinfo);
		list = (ListView) findViewById(R.id.notice_list);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				SwipeInfo info = (SwipeInfo) adapter.getItem(position);
				startToSwipeDetailActivity(info);
			}
		});
	}

	private void startToSwipeDetailActivity(SwipeInfo info) {
		Intent intent = new Intent(this, SwipeDetailActivity.class);

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(JSONConstant.NOTIFICATION_ID, info.getId());
		startActivity(intent);
	}

}