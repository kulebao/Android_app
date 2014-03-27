package com.djc.logintest.activities;

import java.util.List;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.djc.logintest.R;
import com.djc.logintest.adapter.NewsListAdapter;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.customview.MsgListView;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.News;
import com.djc.logintest.handler.MyHandler;
import com.djc.logintest.taskmgr.GetNormalNewsTask;
import com.djc.logintest.utils.MethodUtils;
import com.djc.logintest.utils.Utils;

public class NoticePullRefreshActivity extends UmengStatisticsActivity {
	private NewsListAdapter adapter;
	private MsgListView msgListView;
	private View footer;
	private Handler myhandler;
	private GetNormalNewsTask getNoticeTask;
	private List<News> newsList;
	private boolean bDataChanged = false;
	private ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notice_pull_refresh_list);
		ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.pnotice);
		initDialog();
		initHander();
		initCustomListView();
		loadNewData();
	}

	public void loadNewData() {
		dialog.show();
		refreshHead();
	}

	private void initDialog() {
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage(getResources().getString(R.string.loading_data));
	}

	private boolean runGetNoticeTask(long from, long to, int type) {
		boolean bret = true;
		if (getNoticeTask == null
				|| getNoticeTask.getStatus() != AsyncTask.Status.RUNNING) {
			getNoticeTask = new GetNormalNewsTask(myhandler,
					ConstantValue.GET_NORMAL_NOTICE_MAX_COUNT, from, to, type);
			getNoticeTask.execute();
		} else {
			bret = false;
			Log.d("djc", "should not getNewsImpl task already running!");
		}
		return bret;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 最多保存最新的25条通知
		if (bDataChanged) {
			if (newsList.size() > ConstantValue.GET_NORMAL_NOTICE_MAX_COUNT) {
				newsList = newsList.subList(0,
						ConstantValue.GET_NORMAL_NOTICE_MAX_COUNT);
			}

			DataMgr.getInstance().removeAllNewsByType(
					JSONConstant.NOTICE_TYPE_NORMAL);
			DataMgr.getInstance().addNewsList(newsList);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
	}

	private void initHander() {
		myhandler = new MyHandler(this, dialog) {
			@Override
			public void handleMessage(Message msg) {
				msgListView.onRefreshComplete();
				if (NoticePullRefreshActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.GET_NOTICE_SUCCESS:
					// Toast.makeText(NoticePullRefreshActivity.this,
					// "get suceess!", Toast.LENGTH_SHORT).show();
					handleSuccess(msg);
					footer.setVisibility(View.GONE);
					break;
				case EventType.GET_NOTICE_FAILED:
					Toast.makeText(NoticePullRefreshActivity.this, "获取公告消息失败",
							Toast.LENGTH_SHORT).show();

					footer.setVisibility(View.GONE);
					break;
				default:
					break;
				}
			}
		};
	}

	protected void handleSuccess(Message msg) {
		MethodUtils.removeNewsNotification();
		List<News> list = (List<News>) msg.obj;
		if (!list.isEmpty()) {
			// 刷出新公告了，去掉有新公告的标志
			Utils.saveProp(ConstantValue.HAVE_NEWS_NOTICE, "false");
			bDataChanged = true;
			if (msg.arg1 == ConstantValue.Type_INSERT_HEAD) {
				addToHead(list);
			} else if (msg.arg1 == ConstantValue.Type_INSERT_TAIl) {
				// 旧数据不保存数据库
				newsList.addAll(list);
			} else {
				Log.e("DDD", "handleSuccess bad param arg1=" + msg.arg1);
			}
			adapter.notifyDataSetChanged();
		}
	}

	private void addToHead(List<News> list) {
		// 如果大于等于25条，就说明很可能还有公告没有一次性获取完，为了获取
		// 到连续的公告数据，避免排序和获取复杂化，删除旧的全部公告，只保留最新的25条
		if (list.size() >= ConstantValue.GET_NORMAL_NOTICE_MAX_COUNT) {
			newsList.clear();
			DataMgr.getInstance().removeAllNewsByType(
					JSONConstant.NOTICE_TYPE_NORMAL);
		}

		newsList.addAll(0, list);
		DataMgr.getInstance().addNewsList(list);
	}

	private void initCustomListView() {
		newsList = DataMgr.getInstance().getNewsByType(
				JSONConstant.NOTICE_TYPE_NORMAL,
				ConstantValue.GET_NORMAL_NOTICE_MAX_COUNT);
		adapter = new NewsListAdapter(this, newsList);
		msgListView = (MsgListView) findViewById(R.id.noticelist);// 继承ListActivity，id要写成android.R.id.list，否则报异常
		setRefreshListener();
		msgListView.setAdapter(adapter);
		setItemClickListener();
		setScrollListener();
		addFooter();
	}

	private void setRefreshListener() {
		msgListView
				.setonRefreshListener(new com.djc.logintest.customview.MsgListView.OnRefreshListener() {
					@Override
					public void onRefresh() {
						refreshHead();
					}

				});
	}

	private void refreshHead() {
		long from = 0;
		if (!newsList.isEmpty()) {
			try {
				from = newsList.get(0).getNews_server_id();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		boolean runtask = runGetNoticeTask(from, 0,
				ConstantValue.Type_INSERT_HEAD);
		if (!runtask) {
			// 任务没有执行，立即去掉下拉显示
			msgListView.onRefreshComplete();
		} else {
			// Toast.makeText(NoticePullRefreshActivity.this, "Head Head Head!",
			// Toast.LENGTH_SHORT)
			// .show();
		}
	}

	private void setItemClickListener() {
		msgListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 自定义listview headview占了一个，所以真实数据从1开始
				int currentIndex = position - 1;
				if (currentIndex >= adapter.getCount()) {
					// 当底部条出现时，index会大于count造成数组越界异常，这里处理一下
					return;
				}
				News info = (News) adapter.getItem(currentIndex);
				startTo(info);
			}
		});
	}

	private void setScrollListener() {
		msgListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
				// 0,1都不行，因为有一个隐藏的header，所以必须是大于1才刷新尾部
						&& msgListView.getFirstVisiblePosition() > 1) {
					refreshTail(view);
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
	}

	private void refreshTail(AbsListView view) {
		// 判断是否滚动到底部
		if (view.getLastVisiblePosition() == view.getCount() - 1) {
			Log.d("djc", "on the end!!!!!!!!!!!!!!!!");
			long to = 0;
			if (!newsList.isEmpty()) {
				try {
					to = newsList.get(newsList.size() - 1).getNews_server_id();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			boolean runtask = runGetNoticeTask(0, to,
					ConstantValue.Type_INSERT_TAIl);
			if (runtask) {
				footer.setVisibility(View.VISIBLE);
				// Toast.makeText(NoticePullRefreshActivity.this,
				// "Tail Tail Tail!",
				// Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void addFooter() {
		footer = getLayoutInflater().inflate(R.layout.footerview, null);
		msgListView.addFooterView(footer);
	}

	private void startTo(News info) {
		Intent intent = new Intent(this, NoticeActivity.class);
		// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(JSONConstant.NOTIFICATION_TITLE, info.getTitle());
		intent.putExtra(JSONConstant.NOTIFICATION_BODY, info.getContent());
		intent.putExtra(JSONConstant.TIME_STAMP,
				Utils.formatChineseTime(info.getTimestamp()));
		intent.putExtra(JSONConstant.PUBLISHER, info.getFrom());
		intent.putExtra(JSONConstant.NET_URL, info.getIcon_url());
		intent.putExtra(JSONConstant.LOCAL_URL, info.getNewsLocalIconPath());
		startActivity(intent);
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
			Utils.showTwoBtnResDlg(R.string.delete_all_notice_confirm, this,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							DataMgr.getInstance().removeAllNewsByType(
									JSONConstant.NOTICE_TYPE_NORMAL);
							adapter.clear();
						}
					});
		}
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		loadNewData();
	}

}
