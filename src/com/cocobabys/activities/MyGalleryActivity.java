package com.cocobabys.activities;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.cocobabys.R;
import com.cocobabys.adapter.MyGalleryAdapter;
import com.cocobabys.bean.BusinessInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.customview.SlideGallery;
import com.cocobabys.utils.Utils;

public class MyGalleryActivity extends UmengStatisticsActivity {
	/** Called when the activity is first created. */
	private SlideGallery gallery;
	private MyGalleryAdapter adapter;
	protected int current = 0;
	private TextView countView;
	private List<String> pathList = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_gallery);

		initData();
		countView = (TextView) findViewById(R.id.count);
		initGallery();
	}

	private void initData() {
		String businessInfo = getIntent().getStringExtra(ConstantValue.BUSINESS_INFO);
		Log.d("", "initData info=" + businessInfo);
		BusinessInfo info = JSON.parseObject(businessInfo, BusinessInfo.class);
		pathList = info.getLogoList();
	}

	private void initGallery() {
		adapter = new MyGalleryAdapter(this, pathList);

		gallery = (SlideGallery) findViewById(R.id.mygallery);
		gallery.setVerticalFadingEdgeEnabled(false);
		gallery.setHorizontalFadingEdgeEnabled(false);
		gallery.setAdapter(adapter);

		gallery.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Log.d("DDD", "onItemSelected position=" + position + " id=" + id);
				current = position;
				setIconCount(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		gallery.setSelection(0);
		adapter.notifyDataSetChanged();
	}

	private void setIconCount(int postion) {
		if (postion > adapter.getCount()) {
			postion = adapter.getCount();
		} else if (postion < 0) {
			postion = 0;
		}
		String counts = String.format(Utils.getResString(R.string.icon_count), postion + 1, adapter.getCount());
		countView.setText(counts);
	}

}