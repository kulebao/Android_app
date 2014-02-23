package com.djc.logintest.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
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
import android.widget.Button;
import android.widget.Toast;

import com.djc.logintest.R;
import com.djc.logintest.adapter.ChatListAdapter;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.customview.MsgListView;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.ChatInfo;
import com.djc.logintest.handler.MyHandler;
import com.djc.logintest.taskmgr.GetChatTask;
import com.djc.logintest.utils.Utils;

public class InteractionActivity extends Activity {
	private ProgressDialog dialog;
	private ChatListAdapter adapter;
	private MsgListView msgListView;
	private View footer;
	private Handler myhandler;
	private List<ChatInfo> chatlist;
	private GetChatTask getChatTask;

	// 该参数暂时无效，不用考虑
	private static final String SORT_ASC = "asc";
	private static final String SORT_DESC = "desc";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_pull_refresh_list);
		ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.interaction);
		// test();
		initDialog();
		initBtn();
		initHander();
		initCustomListView();
		loadNewData();
	}

	private void initBtn() {
		Button newchatBtn = (Button) findViewById(R.id.new_chat);
		newchatBtn.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				handleSendBtn();
			}

		});
	}

	private void handleSendBtn() {
		if(!Utils.isNetworkConnected(this)){
			Toast.makeText(this, R.string.net_error, Toast.LENGTH_SHORT).show();
			return;
		}
		
		// 进入到发送chat界面，先取消获取chat的任务，避免重复获取
		if (getChatTask != null
				&& getChatTask.getStatus() == AsyncTask.Status.RUNNING) {
			getChatTask.cancel(true);
		}
		startToSendChatActivity();
	}

	protected void startToSendChatActivity() {
		Intent intent = new Intent(this, SendChatActivity.class);
		startActivityForResult(intent, ConstantValue.START_SEND_CHAT);
	}

	private void test() {
		if (DataMgr.getInstance()
				.getChatInfoWithLimite(ConstantValue.GET_CHATINFO_MAX_COUNT)
				.isEmpty()) {
			addTestData();
		}
	}

	private void addTestData() {
		List<ChatInfo> list = new ArrayList<ChatInfo>();

		for (int i = 0; i < 13; i++) {
			ChatInfo info = new ChatInfo();
			if (i % 3 == 0) {
				info.setSender("sender" + i);
			}
			long currentTimeMillis = System.currentTimeMillis();
			info.setTimestamp(currentTimeMillis + (i + 1) * 60 * 500);
			info.setContent("content " + i);
			if (i % 5 == 0) {
				info.setSend_result(1);
			}
			info.setServer_id(i + 1000);

			list.add(info);
		}

		DataMgr.getInstance().addChatInfoList(list);
	}

	private void loadNewData() {
		dialog.show();
		refreshTailImpl();
	}

	private void initCustomListView() {
		chatlist = DataMgr.getInstance().getChatInfoWithLimite(
				ConstantValue.GET_CHATINFO_MAX_COUNT);
		adapter = new ChatListAdapter(this, chatlist);
		msgListView = (MsgListView) findViewById(R.id.chatlist);// 继承ListActivity，id要写成android.R.id.list，否则报异常
		setRefreshListener();
		msgListView.setAdapter(adapter);
		msgListView.enableTimestamp(false);
		setScrollListener();
		addFooter();
		moveToEndOfList();
	}

	private void moveToEndOfList() {
		// 要减去footer，所以是-2，不是-1
		int endpositon = Math.max(0, chatlist.size() - 2);
		msgListView.setSelection(endpositon);
	}

	private void setRefreshListener() {
		msgListView
				.setonRefreshListener(new com.djc.logintest.customview.MsgListView.OnRefreshListener() {
					@Override
					public void onRefresh() {
						if (chatlist.isEmpty()) {
							refreshTailImpl();
							return;
						}
						refreshHead();
					}

				});
	}

	private void refreshHead() {
		List<ChatInfo> locallist = getChatFromLocal();
		if (!locallist.isEmpty()) {
			chatlist.addAll(0, locallist);
			adapter.notifyDataSetChanged();
			msgListView.onRefreshComplete();
			return;
		}

		refreshHeadFromServer();
	}

	private void refreshHeadFromServer() {
		long to = chatlist.get(0).getServer_id();

		boolean runtask = runGetChatTask(0, to, ConstantValue.Type_INSERT_HEAD,
				SORT_ASC);
		if (!runtask) {
			// 任务没有执行，立即去掉下拉显示
			msgListView.onRefreshComplete();
		} else {
			Toast.makeText(this, "refresh head!", Toast.LENGTH_SHORT).show();
		}
	}

	private boolean runGetChatTask(long from, long to, int type, String sort) {
		boolean bret = true;
		if (getChatTask == null
				|| getChatTask.getStatus() != AsyncTask.Status.RUNNING) {
			getChatTask = new GetChatTask(myhandler,
					ConstantValue.GET_CHATINFO_MAX_COUNT, from, to, type, sort);
			getChatTask.execute();
		} else {
			bret = false;
			Log.d("djc", "should not getChatTask task already running!");
		}
		return bret;
	}

	private List<ChatInfo> getChatFromLocal() {
		if (chatlist != null && !chatlist.isEmpty()) {
			return DataMgr.getInstance().getChatInfoWithLimite(
					ConstantValue.GET_CHATINFO_MAX_COUNT,
					chatlist.get(0).getTimestamp());
		}
		return DataMgr.getInstance().getChatInfoWithLimite(
				ConstantValue.GET_CHATINFO_MAX_COUNT);
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
			boolean runtask = refreshTailImpl();
			if (runtask) {
				footer.setVisibility(View.VISIBLE);
				Toast.makeText(this, "refresh tail!", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	private boolean refreshTailImpl() {
		Log.d("djc", "on the end!!!!!!!!!!!!!!!!");
		long from = 0;
		String sort = SORT_DESC;
		if (!chatlist.isEmpty()) {
			try {
				sort = SORT_ASC;
				from = chatlist.get(chatlist.size() - 1).getServer_id();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		boolean runtask = runGetChatTask(from, 0,
				ConstantValue.Type_INSERT_TAIl, sort);
		return runtask;
	}

	public void addFooter() {
		footer = getLayoutInflater().inflate(R.layout.footerview, null);
		msgListView.addFooterView(footer);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(1, // 组号
				Menu.FIRST, // 唯一的ID号
				Menu.FIRST, // 排序号
				"清空"); // 标题

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == Menu.FIRST) {
			Utils.showTwoBtnResDlg(R.string.delete_all_notice_confirm, this,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							DataMgr.getInstance().removeAllChatInfo();
							adapter.clear();
						}
					});
		}
		return true;
	}

	private void initHander() {
		myhandler = new MyHandler(this, dialog) {
			@Override
			public void handleMessage(Message msg) {
				msgListView.onRefreshComplete();
				footer.setVisibility(View.GONE);
				if (InteractionActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.SUCCESS:
					handleSuccess(msg);
					break;
				case EventType.FAIL:
					Toast.makeText(InteractionActivity.this, "获取数据失败",
							Toast.LENGTH_SHORT).show();

					break;
				default:
					break;
				}
			}
		};
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ConstantValue.START_SEND_CHAT) {
			if (resultCode == ConstantValue.SEND_CHAT_SUCCESS) {
				List<ChatInfo> list = MyApplication.getInstance().getTmpList();
				chatlist.addAll(list);
				adapter.notifyDataSetChanged();
				DataMgr.getInstance().addChatInfoList(list);
			} else if (resultCode == ConstantValue.SEND_CHAT_FAIL) {

			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	protected void handleSuccess(Message msg) {
		List<ChatInfo> list = (List<ChatInfo>) msg.obj;
		if (!list.isEmpty()) {
			// Utils.saveProp(ConstantValue.HAVE_HOMEWORK_NOTICE, "false");
			if (msg.arg1 == ConstantValue.Type_INSERT_HEAD) {
				chatlist.addAll(0, list);
				adapter.notifyDataSetChanged();
			} else if (msg.arg1 == ConstantValue.Type_INSERT_TAIl) {
				chatlist.addAll(list);
				adapter.notifyDataSetChanged();
				moveToEndOfList();
			} else {
				Log.e("DDD", "handleSuccess bad param arg1=" + msg.arg1);
			}

			DataMgr.getInstance().addChatInfoList(list);
		}
	}

	private void initDialog() {
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage(getResources().getString(R.string.loading_data));
	}
}
