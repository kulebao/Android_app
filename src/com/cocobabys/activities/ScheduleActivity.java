package com.cocobabys.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.cocobabys.R;
import com.cocobabys.adapter.ScheduleListAdapter;
import com.cocobabys.bean.ScheduleListItem;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ScheduleInfo;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.taskmgr.GetScheduleTask;
import com.cocobabys.utils.Utils;

public class ScheduleActivity extends UmengStatisticsActivity {
	private ScheduleListAdapter adapter;
	private ListView list;
	private Handler handler;
	private List<ScheduleListItem> scheduleListItem = new ArrayList<ScheduleListItem>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_list);
		ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.schedule);
		initHandler();
		initData();
		runGetScheduleTask();
	}

	private void initHandler() {
		handler = new MyHandler(this, null) {
			@Override
			public void handleMessage(Message msg) {
				if (ScheduleActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.GET_SCHEDULE_SUCCESS:
					Utils.saveProp(ConstantValue.HAVE_SCHEDULE_NOTICE, "false");
					initData();
					break;
				// 已是最新
				case EventType.GET_SCHEDULE_LATEST:
					Utils.saveProp(ConstantValue.HAVE_SCHEDULE_NOTICE, "false");
					break;
				case EventType.NO_SCHEDULE:
					Toast.makeText(ScheduleActivity.this, R.string.no_schedule_yet, Toast.LENGTH_SHORT).show();
					initEmptyData();
					break;
				default:
					break;
				}
			}
		};
	}

	private void initEmptyData() {
		ScheduleInfo scheduleInfo = DataMgr.getInstance().getScheduleInfo();
		// 在本地课程表为空的情况下才执行
		if (scheduleInfo == null) {
			scheduleListItem.clear();
			scheduleListItem = getEmpteyScheduleItemList();
			adapter = new ScheduleListAdapter(this, scheduleListItem);
			list = (ListView) findViewById(R.id.schedule_list);
			list.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
	}

	private List<ScheduleListItem> getEmpteyScheduleItemList() {
		List<ScheduleListItem> list = new ArrayList<ScheduleListItem>();

		Map<Integer, Integer> map = new HashMap<Integer, Integer>() {
			private static final long serialVersionUID = 1L;

			{
				put(0, R.string.mon);
				put(1, R.string.tue);
				put(2, R.string.wed);
				put(3, R.string.thu);
				put(4, R.string.fri);
			}
		};

		for (int i = 0; i < 5; i++) {
			ScheduleListItem item = new ScheduleListItem();
			item.setDayofweek(getResources().getString(map.get(i)));
			list.add(item);
		}
		return list;
	}

	private void initData() {
		ScheduleInfo scheduleInfo = DataMgr.getInstance().getScheduleInfo();
		if (scheduleInfo != null) {
			scheduleListItem.clear();
			scheduleListItem = scheduleInfo.getScheduleListItemList();
			if (!scheduleListItem.isEmpty()) {
				adapter = new ScheduleListAdapter(this, scheduleListItem);
				list = (ListView) findViewById(R.id.schedule_list);
				list.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
		}
	}

	private void runGetScheduleTask() {
		new GetScheduleTask(handler).execute();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d("DDD JJJ", "ScheduleActivity onNewIntent");
		initData();
	}

}
