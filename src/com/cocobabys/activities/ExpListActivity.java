package com.cocobabys.activities;

import java.util.List;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.adapter.ExpListAdapter;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ExpInfo;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.GetExpInfoJob;
import com.cocobabys.jobs.GetSenderInfoJob;
import com.cocobabys.taskmgr.DownloadImgeJob;
import com.cocobabys.utils.Utils;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class ExpListActivity extends UmengStatisticsActivity {
	private static final int CACHE_SIZE = 1 * 1024 * 1024;
	private ProgressDialog dialog;
	private ExpListAdapter adapter;
	private ListView listView;
	private Handler myhandler;
	private List<ExpInfo> explist;

	private DownloadImgeJob downloadImgeJob;
	private GetSenderInfoJob getTeacherInfoJob;
	private int mYear;
	private String mMonth;
	private ImageLoader imageLoader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exp_list);
		initImageLoader();
		initData();
		initBtn();
		initDialog();
		initHander();
		initCustomListView();
		runGetExpByYearAndMonth();
	}

	private void initImageLoader() {
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheOnDisc().imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				.bitmapConfig(Bitmap.Config.RGB_565).cacheInMemory(true)
				.build();
		ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
				this).defaultDisplayImageOptions(defaultOptions)
				.memoryCache(new LruMemoryCache(CACHE_SIZE))
				.memoryCacheSize(CACHE_SIZE);

		ImageLoaderConfiguration config = builder.build();
		imageLoader = ImageLoader.getInstance();

		imageLoader.init(config);
	}

	private void runGetExpByYearAndMonth() {
		dialog.show();
		GetExpInfoJob job = new GetExpInfoJob(myhandler, mYear, mMonth);
		job.execute();
	}

	private void initBtn() {
		TextView refreshBtn = (TextView) findViewById(R.id.rightBtn);
		refreshBtn.setVisibility(View.VISIBLE);
		refreshBtn.setText(Utils.getResString(R.string.refresh));
		refreshBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				runGetExpByYearAndMonth();
			}
		});
	}

	private void initData() {
		mYear = getIntent().getIntExtra(ConstantValue.EXP_YEAR, -1);
		mMonth = getIntent().getStringExtra(ConstantValue.EXP_MONTH);
		Log.d("DJC", "initData mYear=" + mYear + " mMonth=" + mMonth);
	}

	private void initCustomListView() {
		explist = DataMgr.getInstance().getExpInfoByMonthAndYear(
				getYearAndMonth());
		downloadImgeJob = new DownloadImgeJob();
		getTeacherInfoJob = new GetSenderInfoJob();
		adapter = new ExpListAdapter(this, explist, downloadImgeJob,
				getTeacherInfoJob, imageLoader);
		listView = (ListView) findViewById(R.id.explist);// 继承ListActivity，id要写成android.R.id.list，否则报异常
		listView.setAdapter(adapter);
	}

	private String getYearAndMonth() {
		return mYear + "-" + mMonth;
	}

	@Override
	protected void onDestroy() {
		if (downloadImgeJob != null) {
			downloadImgeJob.stopTask();
		}

		if (getTeacherInfoJob != null) {
			getTeacherInfoJob.stopTask();
		}

		if (adapter != null) {
			adapter.releaseCache();
		}
		super.onDestroy();
	}

	private void initHander() {
		myhandler = new MyHandler(this, dialog) {
			@Override
			public void handleMessage(Message msg) {
				if (ExpListActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.GET_EXP_INFO_SUCCESS:
					handleGetExpInfoSuccess();
					break;
				case EventType.GET_EXP_INFO_FAIL:
					Utils.makeToast(ExpListActivity.this, "获取成长经历失败！");
					break;
				default:
					break;
				}
			}
		};
	}

	protected void handleGetExpInfoSuccess() {
		List<ExpInfo> list = DataMgr.getInstance().getExpInfoByMonthAndYear(
				getYearAndMonth());
		adapter.addAll(list);
	}

	private void initDialog() {
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage(getResources().getString(R.string.loading_data));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (MyApplication.getInstance().isForTest()) {
			menu.add(1, // 组号
					Menu.FIRST, // 唯一的ID号
					Menu.FIRST, // 排序号
					"清空"); // 标题
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == Menu.FIRST) {
			DataMgr.getInstance().clearExp();
			adapter.clear();
		}
		return true;
	}
	
}
