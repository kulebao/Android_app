package com.cocobabys.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cocobabys.R;
import com.cocobabys.adapter.EducationGridViewAdapter;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.EducationInfo;
import com.cocobabys.dbmgr.info.InfoHelper;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.taskmgr.GetEducationTask;
import com.cocobabys.utils.Utils;

public class EducationActivity extends UmengStatisticsActivity {
	private static final String RANK = "rank";
	private static final String ITEM_IMAGE = "ItemImage";
	private GridView gridview;
	private ProgressDialog dialog;
	private List<EducationInfo> eduList;
	// 当前显示的在园表现
	private EducationInfo currentEdu;
	private GetEducationTask educationTask;
	private Handler myhandler;
	private boolean bDataChanged = false;
	private EducationGridViewAdapter adapter;
	private ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
	private TextView comentfromView;
	private TextView comentView;
	private TextView commentTimeView;
	private Map<Integer, Integer> explainMap = new HashMap<Integer, Integer>() {
		private static final long serialVersionUID = 1L;

		{
			put(R.drawable.emotion, R.string.emotion_explain);
			put(R.drawable.dining, R.string.dining_explain);
			put(R.drawable.rest, R.string.rest_explain);
			put(R.drawable.manner, R.string.manner_explain);
			put(R.drawable.game, R.string.game_explain);
			put(R.drawable.self_care, R.string.self_care_explain);
			put(R.drawable.exercise, R.string.exercise_explain);
			put(R.drawable.activity, R.string.activity_explain);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.education);

		ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.education);
		initView();
		initDialog();
		initHander();
		loadNewData();
		initBtn();
	}

	private void initBtn() {
		ImageButton leftBtn = (ImageButton) findViewById(R.id.left);
		leftBtn.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handleLeftBtn();
			}
		});

		ImageButton rightBtn = (ImageButton) findViewById(R.id.right);
		rightBtn.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handleRightBtn();
			}
		});
	}

	protected void handleLeftBtn() {
		if (eduList.isEmpty()) {
			runGetEducationTask(0, 0, ConstantValue.Type_INSERT_TAIl);
			return;
		}

		int currentIndex = eduList.indexOf(currentEdu);
		if (currentIndex == eduList.size() - 1) {
			runGetEducationTask(0, currentEdu.getServer_id(), ConstantValue.Type_INSERT_TAIl);
		} else {
			currentEdu = eduList.get(currentIndex + 1);
			refresh();
		}

	}

	protected void handleRightBtn() {
		if (eduList.isEmpty()) {
			runGetEducationTask(0, 0, ConstantValue.Type_INSERT_HEAD);
			return;
		}

		int currentIndex = eduList.indexOf(currentEdu);
		if (currentIndex == 0) {
			runGetEducationTask(currentEdu.getServer_id(), 0, ConstantValue.Type_INSERT_HEAD);
		} else {
			currentEdu = eduList.get(currentIndex - 1);
			refresh();
		}

	}

	private void initHander() {
		myhandler = new MyHandler(this, dialog) {
			@Override
			public void handleMessage(Message msg) {
				if (EducationActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.GET_NOTICE_SUCCESS:
					handleSuccess(msg);
					break;
				case EventType.GET_NOTICE_FAILED:
					Toast.makeText(EducationActivity.this, R.string.load_data_fail, Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
			}
		};
	}

	protected void handleSuccess(Message msg) {
		Utils.saveProp(ConstantValue.HAVE_EDUCATION_NOTICE, "false");
		@SuppressWarnings("unchecked")
		List<EducationInfo> list = (List<EducationInfo>) msg.obj;
		if (!list.isEmpty()) {
			// 刷出新公告了，去掉有新公告的标志
			// Utils.saveProp(ConstantValue.HAVE_NEWS_NOTICE, "false");
			bDataChanged = true;
			if (msg.arg1 == ConstantValue.Type_INSERT_HEAD) {
				addToHead(list);
			} else if (msg.arg1 == ConstantValue.Type_INSERT_TAIl) {
				// 旧数据不保存数据库
				eduList.addAll(list);
			} else {
				Log.e("DDD", "handleSuccess bad param arg1=" + msg.arg1);
			}

			currentEdu = list.get(0);
			refresh();
		} else {
			Toast.makeText(this, R.string.no_more_edu, Toast.LENGTH_SHORT).show();
		}
	}

	private void refresh() {
		buildItem();
		setComment();
		adapter.notifyDataSetChanged();
	}

	private void loadNewData() {
		long from = 0;
		if (!eduList.isEmpty()) {
			try {
				from = eduList.get(0).getServer_id();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		runGetEducationTask(from, 0, ConstantValue.Type_INSERT_HEAD);
	}

	private void runGetEducationTask(long from, int to, int type) {
		if (educationTask == null || educationTask.getStatus() != AsyncTask.Status.RUNNING) {
			dialog.show();
			educationTask = new GetEducationTask(myhandler, ConstantValue.GET_NORMAL_NOTICE_MAX_COUNT, from, to, type);
			educationTask.execute();
		}
	}

	private void initDialog() {
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage(getResources().getString(R.string.loading_data));
	}

	public void initView() {
		gridview = (GridView) findViewById(R.id.gridview);
		initData();
		adapter = new EducationGridViewAdapter(this, lstImageItem);
		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				handleClick(position);
			}
		});
		initCommentView();
	}

	protected void handleClick(int position) {
		@SuppressWarnings("unchecked")
		Map<String, Integer> item = (Map<String, Integer>) adapter.getItem(position);
		int image_id = item.get(ITEM_IMAGE);
		Integer explain_id = explainMap.get(image_id);
		Toast.makeText(this, explain_id, Toast.LENGTH_SHORT).show();
	}

	private void initCommentView() {
		comentfromView = (TextView) findViewById(R.id.comentfrom);
		comentView = (TextView) findViewById(R.id.coment);
		commentTimeView = (TextView) findViewById(R.id.commentTime);
		setComment();
	}

	private void setComment() {
		if (TextUtils.isEmpty(currentEdu.getPublisher())) {
			comentfromView.setVisibility(View.GONE);
		} else {
			comentfromView.setVisibility(View.VISIBLE);
			comentfromView.setText(String.format(getResources().getString(R.string.coment_from),
					currentEdu.getPublisher()));
		}

		comentView.setText(currentEdu.getComments());

		if (currentEdu.getTimestamp() == 0) {
			commentTimeView.setText(getResources().getString(R.string.no_edu));
		} else {
			commentTimeView.setText(InfoHelper.getYearMonthDayFormat().format(new Date(currentEdu.getTimestamp())));
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// savaLatestData();
	}

	private void savaLatestData() {
		// 最多保存最新的25条通知
		if (bDataChanged) {
			if (eduList.size() > ConstantValue.GET_EDU_MAX_COUNT) {
				eduList = eduList.subList(0, ConstantValue.GET_EDU_MAX_COUNT);
			}

			DataMgr.getInstance().removeSelectedChildEduRecord();
			DataMgr.getInstance().addEduRecordList(eduList);
		}
	}

	private void addToHead(List<EducationInfo> list) {
		// 如果大于等于25条，就说明很可能还有数据没有一次性获取完，为了获取
		// 到连续的数据，避免排序和获取复杂化，删除旧的全部数据，只保留最新的25条
		if (list.size() >= ConstantValue.GET_EDU_MAX_COUNT) {
			eduList.clear();
			DataMgr.getInstance().removeSelectedChildEduRecord();
		}

		eduList.addAll(0, list);
		DataMgr.getInstance().addEduRecordList(list);
	}

	public void initData() {
		eduList = DataMgr.getInstance().getSelectedChildEduRecord();

		if (eduList.isEmpty()) {
			// 如果没有数据，则初始化为空的记录
			currentEdu = new EducationInfo();
		} else {
			// 否则获取最近的在园表现
			currentEdu = eduList.get(0);
		}

		buildItem();
	}

	// 修改评分
	private ArrayList<HashMap<String, Object>> buildItem() {
		lstImageItem.clear();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(ITEM_IMAGE, R.drawable.emotion);
		map.put(RANK, currentEdu.getEmotion());
		lstImageItem.add(map);

		map = new HashMap<String, Object>();
		map.put(ITEM_IMAGE, R.drawable.dining);
		map.put(RANK, currentEdu.getDining());
		lstImageItem.add(map);

		map = new HashMap<String, Object>();
		map.put(ITEM_IMAGE, R.drawable.rest);
		map.put(RANK, currentEdu.getRest());
		lstImageItem.add(map);

		map = new HashMap<String, Object>();
		map.put(ITEM_IMAGE, R.drawable.activity);
		map.put(RANK, currentEdu.getActivity());
		lstImageItem.add(map);

		map = new HashMap<String, Object>();
		map.put(ITEM_IMAGE, R.drawable.exercise);
		map.put(RANK, currentEdu.getExercise());
		lstImageItem.add(map);

		map = new HashMap<String, Object>();
		map.put(ITEM_IMAGE, R.drawable.self_care);
		map.put(RANK, currentEdu.getSelf_care());
		lstImageItem.add(map);

		map = new HashMap<String, Object>();
		map.put(ITEM_IMAGE, R.drawable.manner);
		map.put(RANK, currentEdu.getManner());
		lstImageItem.add(map);

		map = new HashMap<String, Object>();
		map.put(ITEM_IMAGE, R.drawable.game);
		map.put(RANK, currentEdu.getGame());
		lstImageItem.add(map);
		return lstImageItem;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (MyApplication.getInstance().isForTest()) {
			if (MyApplication.getInstance().isForTest()) {
				menu.add(1, // 组号
						Menu.FIRST, // 唯一的ID号
						Menu.FIRST, // 排序号
						"清空"); // 标题
			}
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == Menu.FIRST) {
			Utils.showTwoBtnResDlg(R.string.delete_data, this, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					DataMgr.getInstance().removeSelectedChildEduRecord();
					currentEdu = new EducationInfo();
					eduList.clear();
					refresh();
				}
			});
		}
		return true;
	}
}
