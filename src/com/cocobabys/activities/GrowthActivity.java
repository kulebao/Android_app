package com.cocobabys.activities;

import java.util.ArrayList;
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
import com.cocobabys.dbmgr.info.ExpInfo;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.GetExpCountJob;
import com.cocobabys.utils.Utils;

public class GrowthActivity extends UmengStatisticsActivity {
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
		adapter.addAll(list);
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

	private void test() {
		List<GroupExpInfo> expCountGroupByMonthPerYear = DataMgr.getInstance().getExpCountGroupByMonthPerYear(
				currentYear);
		if (expCountGroupByMonthPerYear.size() <= 100) {
			List<ExpInfo> list = getFakeList();
			DataMgr.getInstance().addExpDataList(list);
		}
	}

	private List<ExpInfo> getFakeList() {
		List<ExpInfo> list = new ArrayList<ExpInfo>();
		for (int i = 0; i < 100; i++) {
			ExpInfo info = new ExpInfo();
			info.setChild_id(DataMgr.getInstance().getSelectedChild().getServer_id());
			info.setContent("消息" + i);
			info.setExp_id(i);
			info.setMedium("");
			info.setSender_id(DataMgr.getInstance().getSelfInfoByPhone().getParent_id());
			info.setSender_type(ExpInfo.PARENT_TYPE);

			long timestamp = getTimeStamp(i);
			info.setTimestamp(timestamp);
			list.add(info);
		}
		return list;
	}

	private long getTimeStamp(int i) {
		int month = i % 11;
		int year = 2012;

		if (i > 12) {
			year++;
		}

		if (i > 50) {
			year++;
		}

		if (i > 80) {
			year++;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, i);

		return calendar.getTimeInMillis();
	}

	public void initGridView() {
		gridview = (GridView) findViewById(R.id.gridview);
		List<GroupExpInfo> list = getData();
		adapter = new GrowthGridViewAdapter(this, list);
		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startToExpListActivity(position);
			}
		});
	}

	private void startToExpListActivity(int position) {
		Intent intent = new Intent();
		intent.putExtra(ConstantValue.EXP_YEAR, currentYear);
		selectedMonth = adapter.getItem(position).getMonth();
		intent.putExtra(ConstantValue.EXP_MONTH, selectedMonth);
		intent.setClass(this, ExpListActivity.class);
		startActivity(intent);
	}

	private List<GroupExpInfo> getData() {
		return DataMgr.getInstance().getExpCountGroupByMonthPerYear(currentYear);
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
				runGetExpCountJob();
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
				runGetExpCountJob();
			}
		});

		TextView send = (TextView) findViewById(R.id.rightBtn);
		send.setVisibility(View.VISIBLE);
		send.setText(R.string.send);
		send.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.goNextActivity(GrowthActivity.this, SendExpActivity.class, false);
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

	@Override
	protected void onRestart() {
		int expCountInMonth = DataMgr.getInstance().getExpCountInMonth(currentYear, selectedMonth);
		adapter.changeCount(expCountInMonth, selectedMonth);
		super.onRestart();
	}

}
