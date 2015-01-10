package com.cocobabys.activities;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.cocobabys.R;
import com.cocobabys.adapter.NewChatListAdapter;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.customview.MsgListView;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.NewChatInfo;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.GetChatJob;
import com.cocobabys.jobs.GetSenderInfoJob;
import com.cocobabys.media.MediaMgr;
import com.cocobabys.taskmgr.DownloadImgeJob;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

public class ChatActivity extends UmengStatisticsActivity {
	private ProgressDialog dialog;
	private NewChatListAdapter adapter;
	private MsgListView msgListView;
	private View footer;
	private Handler myhandler;
	private List<NewChatInfo> chatlist;
	private GetChatJob getChatJob;

	private static final int IMAGE_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	private static final int CHECK_ICON_CODE = 2;
	private static final int START_SEND_CHAT = 3;

	private static final String TMP_BMP = "tmp_bmp_for_chat.jpg";
	private Uri uri;
	private DownloadImgeJob downloadImgeJob;
	private GetSenderInfoJob getTeacherInfoJob;
	private String childid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_pull_refresh_list);
		childid = DataMgr.getInstance().getSelectedChild().getServer_id();
		// test();
		// initImageUri();
		initDialog();
		initBtn();
		initHander();
		initCustomListView();
		loadNewData();
	}

	private void initBtn() {
		ImageView newchatBtn = (ImageView) findViewById(R.id.new_chat);
		newchatBtn.setOnClickListener(new MyClickListner() {
			@Override
			public void execute() {
				startToSendChatActivity();
			}

		});

		ImageView camera = (ImageView) findViewById(R.id.camera);
		camera.setOnClickListener(new MyClickListner() {

			@Override
			public void execute() {
				if (!Utils.isSdcardExisting()) {
					Toast.makeText(ChatActivity.this, "未找到存储卡，无法保存图片！",
							Toast.LENGTH_LONG).show();
					return;
				}
				chooseIconFromCamera();
			}

		});

		ImageView gallery = (ImageView) findViewById(R.id.gallery);
		gallery.setOnClickListener(new MyClickListner() {

			@Override
			public void execute() {
				chooseIconFromGallery();
			}

		});
	}

	private void startToCheckIconActivity(String url) {
		Intent intent = new Intent(this, NewCheckIconActivity.class);
		intent.putExtra(ConstantValue.TMP_CHAT_PATH, url);
		startActivityForResult(intent, CHECK_ICON_CODE);
	}

	private void chooseIconFromGallery() {
		Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
		galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
		galleryIntent.setType("image/*");
		startActivityForResult(galleryIntent, IMAGE_REQUEST_CODE);
	}

	private void chooseIconFromCamera() {
		File file = getFile();
		uri = Uri.fromFile(file);
		Log.d("DJC", "path =" + uri.getPath());
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
		startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
	}

	private File getFile() {
		String path = Utils.getDefaultCameraDir();
		File file = new File(path, System.currentTimeMillis() + ".jpg");
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	// private void initImageUri() {
	// uri = Uri.fromFile(new File(Utils.getSDCardFileDir(Utils.APP_DIR_TMP),
	// TMP_BMP));
	// }

	// 拷贝图库图片到sd卡上
	// private void saveBitmap(Intent data) {
	// Uri currentUri = data.getData();
	// Bitmap bitmap = null;
	// ContentResolver cr = this.getContentResolver();
	// try {
	// BitmapFactory.Options options = new BitmapFactory.Options();
	// options.inJustDecodeBounds = true;
	// BitmapFactory.decodeStream(cr.openInputStream(currentUri), null,
	// options);
	// options.inSampleSize = ImageDownloader.computeSampleSize(options,
	// -1, ImageDownloader.getMaxPix());
	// options.inJustDecodeBounds = false;
	//
	// bitmap = BitmapFactory.decodeStream(cr.openInputStream(currentUri),
	// null, options);
	// Utils.saveBitmapToSDCard(bitmap, uri.getPath());
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// if (bitmap != null) {
	// bitmap.recycle();
	// }
	// }
	// }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case START_SEND_CHAT:
			handleSendChat();
			break;
		case IMAGE_REQUEST_CODE:
			if (Utils.isSdcardExisting()) {
				String path = getPath(data);

				Log.d("", "IMAGE_REQUEST_CODE url" + path);
				startToCheckIconActivity(path);
			}
			break;
		case CAMERA_REQUEST_CODE:
			Log.d("", "CAMERA_REQUEST_CODE url" + uri.getPath());
			startToCheckIconActivity(uri.getPath());
			break;
		case CHECK_ICON_CODE:
			handleSendChat();
			break;

		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private String getPath(Intent data) {
		Cursor cursor = null;
		String path = "";
		try {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			cursor = getContentResolver().query(selectedImage, filePathColumn,
					null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			path = cursor.getString(columnIndex);
		} catch (Exception e) {
			DataUtils.closeCursor(cursor);
		}
		return path;
	}

	protected void startToSendChatActivity() {
		Intent intent = new Intent(this, SendNewChatActivity.class);
		startActivityForResult(intent, START_SEND_CHAT);
	}

	private void loadNewData() {
		dialog.setMessage(getResources().getString(R.string.loading_data));
		dialog.show();
		refreshTailImpl();
	}

	private void initCustomListView() {
		chatlist = DataMgr.getInstance().getNewChatInfoWithLimite(
				ConstantValue.GET_CHATINFO_MAX_COUNT, childid);
		downloadImgeJob = new DownloadImgeJob();
		getTeacherInfoJob = new GetSenderInfoJob();
		adapter = new NewChatListAdapter(this, chatlist, downloadImgeJob,
				getTeacherInfoJob);

		msgListView = (MsgListView) findViewById(R.id.chatlist);// 继承ListActivity，id要写成android.R.id.list，否则报异常
		setRefreshListener();
		msgListView.setAdapter(adapter);
		msgListView.enableTimestamp(false);

		setScrollListener();
		addFooter();
		moveToEndOfList();
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

	@Override
	protected void onStop() {
		MediaMgr.close();
		super.onStop();
	}

	private void moveToEndOfList() {
		// 要减去footer，所以是-2，不是-1
		int endpositon = Math.max(0, chatlist.size() - 2);
		msgListView.setSelection(endpositon);
	}

	private void setRefreshListener() {
		msgListView
				.setonRefreshListener(new com.cocobabys.customview.MsgListView.OnRefreshListener() {
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
		List<NewChatInfo> locallist = getChatFromLocal();
		if (!locallist.isEmpty()) {
			chatlist.addAll(0, locallist);
			adapter.notifyDataSetChanged();
			msgListView.onRefreshComplete();
			return;
		}

		refreshHeadFromServer();
	}

	private void refreshHeadFromServer() {
		long to = chatlist.get(0).getChat_id();

		boolean runtask = runGetChatTask(0, to, ConstantValue.Type_INSERT_HEAD);
		if (!runtask) {
			// 任务没有执行，立即去掉下拉显示
			msgListView.onRefreshComplete();
		} else {
			// Toast.makeText(this, "refresh head!", Toast.LENGTH_SHORT).show();
		}
	}

	private boolean runGetChatTask(long from, long to, int type) {
		boolean bret = true;
		if (getChatJob == null || getChatJob.isDone()) {
			getChatJob = new GetChatJob(myhandler,
					ConstantValue.GET_CHATINFO_MAX_COUNT, from, to, type,
					childid);
			getChatJob.execute();
		} else {
			bret = false;
			Log.d("djc", "should not getChatTask task already running!");
		}
		return bret;
	}

	private List<NewChatInfo> getChatFromLocal() {
		if (chatlist != null && !chatlist.isEmpty()) {
			return DataMgr.getInstance().getNewChatInfoWithLimite(
					ConstantValue.GET_CHATINFO_MAX_COUNT,
					chatlist.get(0).getTimestamp(), childid);
		}
		return DataMgr.getInstance().getNewChatInfoWithLimite(
				ConstantValue.GET_CHATINFO_MAX_COUNT, childid);
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
				// Toast.makeText(this, "refresh tail!", Toast.LENGTH_SHORT)
				// .show();
			}
		}
	}

	private boolean refreshTailImpl() {
		Log.d("djc", "on the end!!!!!!!!!!!!!!!!");
		long from = 0;
		if (!chatlist.isEmpty()) {
			try {
				from = chatlist.get(chatlist.size() - 1).getChat_id();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		boolean runtask = runGetChatTask(from, 0,
				ConstantValue.Type_INSERT_TAIl);
		return runtask;
	}

	public void addFooter() {
		footer = getLayoutInflater().inflate(R.layout.footerview, null);
		msgListView.addFooterView(footer);
	}

	private void initHander() {
		myhandler = new MyHandler(this, dialog) {
			@Override
			public void handleMessage(Message msg) {
				msgListView.onRefreshComplete();
				footer.setVisibility(View.GONE);
				if (ChatActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.GET_CHAT_SUCCESS:
					handleSuccess(msg);
					break;
				case EventType.GET_CHAT_FAIL:
					Toast.makeText(ChatActivity.this, "获取数据失败",
							Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
			}
		};
	}

	private void handleSendChat() {
		List<NewChatInfo> list = MyApplication.getInstance()
				.getTmpNewChatList();
		int size = chatlist.size();

		removeDuplicatieInfo(list);

		chatlist.addAll(list);
		adapter.notifyDataSetChanged();

		if (!list.isEmpty()) {
			// 减去footer
			size -= 1;
			// 把焦点移到当前最后一条再后面一条，也就是新增的第一条
			size += 1;
			msgListView.setSelection(size);
		}

		DataMgr.getInstance().addNewChatInfoList(list);
	}

	protected void handleSuccess(Message msg) {
		DataUtils.saveProp(ConstantValue.HAVE_CHAT_NOTICE, "false");
		List<NewChatInfo> list = (List<NewChatInfo>) msg.obj;

		if (!list.isEmpty()) {
			removeDuplicatieInfo(list);
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

			DataMgr.getInstance().addNewChatInfoList(list);
		} else {
			Toast.makeText(this, R.string.no_more_chat, Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void removeDuplicatieInfo(List<NewChatInfo> list) {
		Iterator<NewChatInfo> iterator = list.iterator();

		while (iterator.hasNext()) {
			NewChatInfo next = iterator.next();
			if (chatlist.contains(next)) {
				iterator.remove();
			}
		}
	}

	private void initDialog() {
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
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
			DataMgr.getInstance().removeAllNewChatInfo();
			adapter.clear();
		}
		return true;
	}

	private abstract class MyClickListner implements
			android.view.View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (!Utils.isNetworkConnected(ChatActivity.this)) {
				Toast.makeText(ChatActivity.this, R.string.net_error,
						Toast.LENGTH_SHORT).show();
				return;
			}

			// 启动到发送界面时，取消获取任务，以发送后免重复获取
			if (getChatJob != null && !getChatJob.isDone()) {
				getChatJob.cancel(true);
			}
			execute();
		}

		public abstract void execute();
	}
}
