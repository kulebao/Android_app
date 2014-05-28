package com.cocobabys.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.adapter.SlideGalleryAdapter;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.customview.SlideGallery;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ExpInfo;
import com.cocobabys.taskmgr.DownloadImgeJob;
import com.cocobabys.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SlideGalleryActivity extends Activity {
	/** Called when the activity is first created. */
	private SlideGallery gallery;
	private ImageLoader imageLoader;
	private SlideGalleryAdapter adapter;
	private TextView contentView;
	protected int current = 0;
	private TextView countView;
	private ExpInfo info;
	private DownloadImgeJob downloadImgeJob;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.slide_gallery);

		initData();
		if (info == null) {
			Log.e("Bad request!", "ExpInfo is null!");
			return;
		}

		initImageLoader();
		initUI();
	}

	private void initData() {
		long exp_id = getIntent().getLongExtra(ConstantValue.EXP_ID, -1);
		info = DataMgr.getInstance().getExpInfoByID(exp_id);
	}

	private void initUI() {
		// 去掉删除和确定按钮
		findViewById(R.id.bottom).setVisibility(View.INVISIBLE);
		initGallery();
		initTextView();
	}

	private void initTextView() {
		contentView = (TextView) findViewById(R.id.content);
		contentView.setText(info.getContent());
		countView = (TextView) findViewById(R.id.count);
	}

	private void initGallery() {
		gallery = (SlideGallery) findViewById(R.id.mygallery);
		gallery.setVerticalFadingEdgeEnabled(false);
		gallery.setHorizontalFadingEdgeEnabled(false);
		downloadImgeJob = new DownloadImgeJob();
		adapter = new SlideGalleryAdapter(this, imageLoader, info,
				downloadImgeJob);

		gallery.setAdapter(adapter);

		gallery.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d("DDD", "onItemSelected position=" + position + " id="
						+ id);
				current = position;
				setIconCount(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

	}

	private void setIconCount(int postion) {
		if (postion > adapter.getCount()) {
			postion = adapter.getCount();
		} else if (postion < 0) {
			postion = 0;
		}
		String counts = String.format(Utils.getResString(R.string.icon_count),
				postion + 1, adapter.getCount());
		countView.setText(counts);
	}

	@Override
	protected void onDestroy() {
		adapter.clearCache();
		if (downloadImgeJob != null) {
			downloadImgeJob.stopTask();
		}
		super.onDestroy();
	}

	private void initImageLoader() {
		imageLoader = ImageLoader.getInstance();
	}

}