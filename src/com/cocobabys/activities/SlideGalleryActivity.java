package com.cocobabys.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cocobabys.R;
import com.cocobabys.adapter.SlideGalleryAdapter;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.NoticeAction;
import com.cocobabys.customview.SlideGallery;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ExpInfo;
import com.cocobabys.taskmgr.DownloadImgeJob;
import com.cocobabys.utils.Utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.TextView;

public class SlideGalleryActivity extends UmengStatisticsActivity {
	/** Called when the activity is first created. */
	private SlideGallery gallery;
	private SlideGalleryAdapter adapter;
	private TextView contentView;
	protected int current = 0;
	private TextView countView;
	private ExpInfo info;
	private DownloadImgeJob downloadImgeJob;
	private String action;
	private String content;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.slide_gallery);

		action = getIntent().getAction();
		if (action == null) {
			finish();
			return;
		}

		init();
	}

	private void init() {
		if (NoticeAction.ACTION_GALLERY_READ_ONLY.equals(action)) {
			long exp_id = getIntent().getLongExtra(ConstantValue.EXP_ID, -1);
			info = DataMgr.getInstance().getExpInfoByID(exp_id);
			content = info.getContent();
			// 去掉删除和确定按钮
			findViewById(R.id.delete).setVisibility(View.INVISIBLE);
			findViewById(R.id.confirm).setVisibility(View.INVISIBLE);
			initBtn();
			initReadOnlyGallery();

		} else if (NoticeAction.ACTION_GALLERY_CAN_DELETE.equals(action)) {
			content = getIntent().getStringExtra(NoticeAction.EXP_TEXT);
			List<String> pathList = getList();
			// 去掉保存按钮
			findViewById(R.id.save).setVisibility(View.INVISIBLE);
			initCanDeleteGallery(pathList);
			initBtn();
		}
		initTextView();
	}

	private List<String> getList() {
		String[] pathArray = getIntent().getStringArrayExtra(
				NoticeAction.SELECTED_PATH);

		List<String> pathList = new ArrayList<String>();
		if (pathArray != null) {
			Collections.addAll(pathList, pathArray);
		}
		return pathList;
	}

	private void initBtn() {
		Button delete = (Button) findViewById(R.id.delete);
		delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (current >= 0) {
					int postion = current;

					adapter.remove(current);
					current--;

					if (current < 0) {
						current = 0;
					}

					if (adapter.getCount() == 0) {
						Intent data = new Intent().putExtra(
								NoticeAction.PATH_AFTER_CHANGE, new String[0]);
						setResult(RESULT_OK, data);
						SlideGalleryActivity.this.finish();
						return;
					}

					setIconCount(postion);
				}
			}
		});

		Button confirm = (Button) findViewById(R.id.confirm);
		confirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				List<String> list = adapter.getList();
				String[] array = list.toArray(new String[list.size()]);
				Intent data = new Intent().putExtra(
						NoticeAction.PATH_AFTER_CHANGE, array);
				setResult(RESULT_OK, data);
				SlideGalleryActivity.this.finish();
			}
		});

		Button save = (Button) findViewById(R.id.save);
		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleAddToGallery();
			}
		});
	}

	private void handleAddToGallery() {
		try {
			File file = new File(adapter.getList().get(current));
			Utils.addPicToGallery(Uri.fromFile(file));
			Utils.makeToast(SlideGalleryActivity.this, R.string.copy_to_gallery);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initTextView() {
		contentView = (TextView) findViewById(R.id.content);
		contentView.setText(content);
		countView = (TextView) findViewById(R.id.count);
	}

	private void initCanDeleteGallery(List<String> pathList) {
		adapter = new SlideGalleryAdapter(this, pathList);
		initGallery();

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

	private void initGallery() {
		gallery = (SlideGallery) findViewById(R.id.mygallery);
		gallery.setVerticalFadingEdgeEnabled(false);
		gallery.setHorizontalFadingEdgeEnabled(false);
		gallery.setAdapter(adapter);
		
		int pos = getIntent().getIntExtra(NoticeAction.GALLERY_POSITION, 0);
		
		Log.d("DDDDDD", "initGallery pos=" + pos);
		gallery.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
			}
		});
		
		gallery.setSelection(pos);
		adapter.notifyDataSetChanged();
	}

	private void initReadOnlyGallery() {
		downloadImgeJob = new DownloadImgeJob();
		adapter = new SlideGalleryAdapter(this, info, downloadImgeJob);

		initGallery();
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
		if (downloadImgeJob != null) {
			downloadImgeJob.stopTask();
		}
		super.onDestroy();
	}

}