package com.cocobabys.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.SwipeInfo;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.noticepaser.SwapCardNoticePaser;
import com.cocobabys.taskmgr.DownLoadImgAndSaveTask;
import com.cocobabys.utils.Utils;

public class SwipeDetailActivity extends UmengStatisticsActivity {
	private TextView contentView;
	private ImageView noticeiconView;
	private TextView timeView;
	private Handler handler;
	private SwipeInfo swipeinfo;
	private AsyncTask<Void, Void, Integer> downloadIconTask;
	private TextView swipefromview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notice);
		Log.d("DDD JJJ", "SwipeDetailActivity onCreate");
		initHandler();
		initView();
		setData(getIntent());
	}

	private void initHandler() {
		handler = new MyHandler(this, null) {
			@Override
			public void handleMessage(Message msg) {
				if (SwipeDetailActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.DOWNLOAD_IMG_SUCCESS:
					setIcon();
					break;
				default:
					break;
				}
			}

		};
	}

	private void setData(Intent intent) {
		int id = intent.getIntExtra(JSONConstant.NOTIFICATION_ID, -1);
		swipeinfo = DataMgr.getInstance().getSwipeDataByID(id);
		try {
			if (swipeinfo != null) {
				setIcon();
				setPublisher();
				setContent();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void setIcon() {
		Bitmap bmp = Utils.getLoacalBitmap(SwapCardNoticePaser
				.createSwipeIconPath(String.valueOf(swipeinfo.getTimestamp())));
		if (bmp != null) {
			noticeiconView.setVisibility(View.VISIBLE);
			Utils.setImg(noticeiconView, bmp);
		} else {
			noticeiconView.setVisibility(View.VISIBLE);
			noticeiconView.setImageResource(R.drawable.default_icon);
			runDownloadIconTask();
		}
	}

	private void runDownloadIconTask() {
		if (downloadIconTask != null
				&& downloadIconTask.getStatus() == AsyncTask.Status.RUNNING) {
			// 后执行的取消先执行的
			downloadIconTask.cancel(true);
		}

		downloadIconTask = new DownLoadImgAndSaveTask(handler,
				swipeinfo.getUrl(),
				SwapCardNoticePaser.createSwipeIconPath(String
						.valueOf(swipeinfo.getTimestamp()))).execute();
	}

	public void setContent() {
		contentView.setText(swipeinfo.getNoticeTitle()
				+ "\n"
				+ swipeinfo.getNoticeBody(DataMgr.getInstance()
						.getSelectedChild().getChild_nick_name()));
	}

	public void setTimestamp() {
		timeView.setText(swipeinfo.getFormattedTime());
	}

	public void setPublisher() {
		try {
			swipefromview.setVisibility(View.VISIBLE);
			swipefromview.setText(DataMgr.getInstance().getSchoolInfo()
					.getSchool_name());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initView() {
		timeView = (TextView) findViewById(R.id.time);
		contentView = (TextView) findViewById(R.id.noticecontent);
		noticeiconView = (ImageView) findViewById(R.id.noticeicon);
		swipefromview = (TextView) findViewById(R.id.swipefrom);
		TextView titleView = (TextView) findViewById(R.id.title);
		titleView.setVisibility(View.GONE);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d("DDD JJJ", "SwipeDetailActivity onNewIntent");
		setIntent(intent);
		setData(intent);
	}

}
