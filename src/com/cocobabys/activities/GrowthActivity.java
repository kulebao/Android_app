package com.cocobabys.activities;

import java.util.Calendar;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.adapter.GrowthGridViewAdapter;
import com.cocobabys.bean.GroupExpInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.NativeMediumInfo;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.GetExpCountJob;
import com.cocobabys.utils.Utils;

public class GrowthActivity extends UmengStatisticsActivity {
	private static final int START_TO_EXP_LIST = 1;
	private TextView titleView;
	private int currentYear = Calendar.getInstance().get(Calendar.YEAR);
	private Button leftBtn;
	private Button rightBtn;
	private GridView gridview;
	private GrowthGridViewAdapter adapter;
	private ProgressDialog dialog;
	private Handler handler;
	private String selectedMonth;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.grouth);
		initUI();
		initHandler();
		runGetExpCountJob();

		// test
		{
			List<NativeMediumInfo> allNativeMediumInfo = DataMgr.getInstance()
					.getAllNativeMediumInfo();
			Log.d("", "dDD getAllNativeMediumInfo");
			for (NativeMediumInfo info : allNativeMediumInfo) {
				Log.d("", "dDD info =" + info.toString());
			}
		}
	}

	private void runGetExpCountJob() {
		dialog.show();
		GetExpCountJob getExpCountJob = new GetExpCountJob(handler, currentYear);
		getExpCountJob.execute();
	}

	private void initHandler() {
		handler = new MyHandler(this, dialog) {

			@Override
			public void handleMessage(Message msg) {
				if (GrowthActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.GET_EXP_COUNT_FAIL:
					Utils.makeToast(GrowthActivity.this, "获取数量失败");
					break;
				case EventType.GET_EXP_COUNT_SUCCESS:
					handleGetCountSuccess((List<GroupExpInfo>) msg.obj);
					break;
				default:
					break;
				}
			}
		};
	}

	protected void handleGetCountSuccess(List<GroupExpInfo> list) {
		adapter.updateList(list);
	}

	private void initDialog() {
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage(getResources().getString(R.string.refresh_data));
	}

	private void initUI() {
		initDialog();
		titleView = (TextView) findViewById(R.id.topYear);
		setTitle();
		initBtn();
		initGridView();
	}

	public void initGridView() {
		gridview = (GridView) findViewById(R.id.gridview);
		List<GroupExpInfo> list = getData();
		adapter = new GrowthGridViewAdapter(this, list);
		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				startToExpListActivity(position);
			}
		});
	}

	@Override
	// 处理从图库和拍照返回的照片
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == START_TO_EXP_LIST) {
			List<GroupExpInfo> list = getData();
			adapter.addAll(list);
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void startToExpListActivity(int position) {
		Intent intent = new Intent();
		intent.putExtra(ConstantValue.EXP_YEAR, currentYear);
		selectedMonth = adapter.getItem(position).getMonth();
		intent.putExtra(ConstantValue.EXP_MONTH, selectedMonth);
		intent.setClass(this, ExpListActivity.class);
		startActivityForResult(intent, START_TO_EXP_LIST);
	}

	private List<GroupExpInfo> getData() {
		return DataMgr.getInstance()
				.getExpCountGroupByMonthPerYear(currentYear);
	}

	private void setTitle() {
		titleView.setText(String.valueOf(currentYear));
	}

	private void initBtn() {
		leftBtn = (Button) findViewById(R.id.leftArrow);
		leftBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentYear <= 2012) {
					return;
				}
				currentYear--;
				setTitle();
				adapter.addAll(getData());
				// runGetExpCountJob();
			}
		});

		rightBtn = (Button) findViewById(R.id.rightArrow);

		rightBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentYear >= 2200) {
					return;
				}
				currentYear++;
				setTitle();
				adapter.addAll(getData());
				// runGetExpCountJob();
			}
		});

		Button send = (Button) findViewById(R.id.rightBtn);
		send.setVisibility(View.VISIBLE);
		send.setText(R.string.add);
		send.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.goNextActivity(GrowthActivity.this,
						SendExpActivity.class, false);
			}
		});

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
