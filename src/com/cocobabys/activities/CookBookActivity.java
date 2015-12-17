package com.cocobabys.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cocobabys.R;
import com.cocobabys.adapter.CookBookListAdapter;
import com.cocobabys.bean.CookbookItem;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.CookBookInfo;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.taskmgr.GetCookbookTask;
import com.cocobabys.utils.DataUtils;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

public class CookBookActivity extends UmengStatisticsActivity {
	private CookBookListAdapter adapter;
	private ListView list;
	private Handler handler;
	private List<CookbookItem> cookbookItems = new ArrayList<CookbookItem>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cook_list);
		ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.cookbook_notice);
		initHandler();
		initData();
		runGetCookBookTask();
	}

	private void initHandler() {
		handler = new MyHandler(this, null) {
			@Override
			public void handleMessage(Message msg) {
				if (CookBookActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.GET_COOKBOOK_SUCCESS:
					// 获取到新的食谱，将新食谱的标志置为false
					DataUtils.saveProp(ConstantValue.HAVE_COOKBOOK_NOTICE, "false");
					initData();
					break;
				// 已是最新
				case EventType.GET_COOKBOOK_LATEST:
					DataUtils.saveProp(ConstantValue.HAVE_COOKBOOK_NOTICE, "false");
					break;
				case EventType.NO_COOKBOOK:
					Toast.makeText(CookBookActivity.this, R.string.no_cookbook_yet, Toast.LENGTH_SHORT).show();
					initEmptyData();
					break;
				default:
					break;
				}
			}
		};
	}

	private void initEmptyData() {
		CookBookInfo cookBookInfo = DataMgr.getInstance().getCookBookInfo();
		// 在本地食谱为空的情况下才执行
		if (cookBookInfo == null) {
			cookbookItems.clear();
			cookbookItems = getEmpteyCookbookItems();
			adapter = new CookBookListAdapter(this, cookbookItems);
			list = (ListView) findViewById(R.id.cook_list);
			list.setAdapter(adapter);
			adapter.notifyDataSetChanged();
		}
	}

	private List<CookbookItem> getEmpteyCookbookItems() {
		List<CookbookItem> list = new ArrayList<CookbookItem>();

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
			CookbookItem item = new CookbookItem();
			item.setCookWeek(getResources().getString(map.get(i)));
			list.add(item);
		}
		return list;
	}

	private void initData() {
		CookBookInfo cookBookInfo = DataMgr.getInstance().getCookBookInfo();
		if (cookBookInfo != null) {
			cookbookItems.clear();
			cookbookItems = cookBookInfo.getCookbookItemList();
			if (!cookbookItems.isEmpty()) {
				adapter = new CookBookListAdapter(this, cookbookItems);
				list = (ListView) findViewById(R.id.cook_list);
				list.setAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
		}
	}

	private void runGetCookBookTask() {
		new GetCookbookTask(handler).execute();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d("DDD JJJ", "ScheduleActivity onNewIntent");
		initData();
	}
}
