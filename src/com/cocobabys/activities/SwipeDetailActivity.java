package com.cocobabys.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.bean.AdInfo;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.SwipeInfo;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.taskmgr.DownLoadImgAndSaveTask;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

public class SwipeDetailActivity extends UmengStatisticsActivity {
	private TextView contentView;
	private ImageView noticeiconView;
	private TextView timeView;
	private Handler handler;
	private SwipeInfo swipeinfo;
	private AsyncTask<Void, Void, Integer> downloadIconTask;
	private TextView swipefromview;
	private AdInfo adInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notice);
		Log.d("DDD JJJ", "SwipeDetailActivity onCreate");
		initHandler();
		initView();
		setData(getIntent());
	}

	private void setAD() {
		if (adInfo != null && DataUtils.isFileExist(adInfo.getLocalFileName())) {
			ImageView adimageView = (ImageView) findViewById(R.id.adimage);
			adimageView.setVisibility(View.VISIBLE);
			Bitmap loacalBitmap = Utils.getLoacalBitmap(adInfo
					.getLocalFileName());
			Utils.setImg(adimageView, loacalBitmap);

			if (!TextUtils.isEmpty(adInfo.getLink())) {
				adimageView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						launchBrowser(adInfo.getLink());
					}
				});
			}
		}
	}

	public void launchBrowser(String url) {
		try {
			Log.d("DJC GET ", "url = " + url);
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
				case EventType.DOWNLOAD_FILE_SUCCESS:
					setIcon();
					break;
				default:
					break;
				}
			}

		};
	}

	private void setData(Intent intent) {
		long id = intent.getLongExtra(JSONConstant.NOTIFICATION_ID, -1L);
		swipeinfo = DataMgr.getInstance().getSwipeDataByTimeStamp(id);
		try {
			if (swipeinfo != null) {
				adInfo = DataUtils.getAdInfo();
				setIcon();
				setPublisher();
				setContent();
				setAD();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setIcon() {
		Bitmap bmp = Utils.getLoacalBitmap(swipeinfo.getSwipeLocalIconPath());
		if (bmp != null) {
			noticeiconView.setVisibility(View.VISIBLE);
			Utils.setImg(noticeiconView, bmp);
		} else {
			if (!TextUtils.isEmpty(swipeinfo.getUrl())) {
				noticeiconView.setVisibility(View.VISIBLE);
				noticeiconView.setImageResource(R.drawable.default_icon);
				runDownloadIconTask();
			}
		}
	}

	private void runDownloadIconTask() {
		if (downloadIconTask != null
				&& downloadIconTask.getStatus() == AsyncTask.Status.RUNNING) {
			// 后执行的取消先执行的
			downloadIconTask.cancel(true);
		}

		downloadIconTask = new DownLoadImgAndSaveTask(handler,
				swipeinfo.getUrl(), swipeinfo.getSwipeLocalIconPath())
				.execute();
	}

	public void setContent() {
		StringBuffer content = new StringBuffer();

		content.append(swipeinfo.getNoticeTitle() + "\n\n");

		if (adInfo != null) {
			content.append(Utils.getAdNotice(adInfo.getName()));
		}

		content.append(swipeinfo.getNoticeBody(DataMgr.getInstance()
				.getSelectedChild().getChild_nick_name()));

		contentView.setText(content.toString());
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
