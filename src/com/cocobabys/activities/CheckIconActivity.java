package com.cocobabys.activities;

import java.util.List;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cocobabys.R;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.NewChatInfo;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.UploadChatIconJob;
import com.cocobabys.utils.ImageDownloader;

public class CheckIconActivity extends UmengStatisticsActivity {

	private Bitmap bitmap;
	private Handler handler;
	private ProgressDialog dialog;
	private String childid;
	private String path;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.check_icon);
		childid = DataMgr.getInstance().getSelectedChild().getServer_id();
		path = getIntent().getStringExtra(ConstantValue.TMP_CHAT_PATH);

		showIcon();
		initBtn();
		initDialog();
		initHandler();
	}

	private void initDialog() {
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage(getResources().getString(R.string.sending));
	}

	private void initHandler() {
		handler = new MyHandler(this, dialog) {
			@Override
			public void handleMessage(Message msg) {
				if (CheckIconActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.SEND_CHAT_SUCCESS:
					handleSuccess(msg);
					break;
				case EventType.SEND_CHAT_FAIL:
					Toast.makeText(CheckIconActivity.this,
							R.string.send_icon_fail, Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
			}

		};
	}

	@SuppressWarnings("unchecked")
	private void handleSuccess(Message msg) {
		Toast.makeText(CheckIconActivity.this, R.string.send_icon_success,
				Toast.LENGTH_SHORT).show();
		MyApplication.getInstance().setTmpNewChatList(
				(List<NewChatInfo>) msg.obj);
		setResult(ConstantValue.SEND_CHAT_SUCCESS);
		CheckIconActivity.this.finish();
	}

	private void initBtn() {
		TextView send = (TextView) findViewById(R.id.send);
		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				runSendPicTask();
			}
		});

		TextView cancel = (TextView) findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CheckIconActivity.this.finish();
			}
		});
	}

	protected void runSendPicTask() {
		long lastid = DataMgr.getInstance().getLastNewChatServerid(childid);
		UploadChatIconJob uploadChatIconTask = new UploadChatIconJob(handler,
				bitmap, lastid, childid, path);
		uploadChatIconTask.execute();
		dialog.show();
	}

	private void showIcon() {
		ImageView image = (ImageView) findViewById(R.id.imageview);
		int maxPixel = getMaxPix();

		bitmap = getResizedBmp(maxPixel, path);
		image.setImageBitmap(bitmap);
	}

	private int getMaxPix() {
		DisplayMetrics dm = new DisplayMetrics();
		dm = getResources().getDisplayMetrics();

		Log.d("DDD", "w = " + dm.widthPixels + " h=" + dm.heightPixels
				+ " density=" + dm.density);
		int maxPixel = (int) (dm.widthPixels * dm.heightPixels * dm.density);
		return maxPixel;
	}

	private Bitmap getResizedBmp(int maxPixel, String path) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = ImageDownloader.computeSampleSize(options, -1,
				maxPixel);
		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(path, options);
		return bitmap;
	}

	@Override
	protected void onDestroy() {
		if (bitmap != null) {
			bitmap.recycle();
		}
		super.onDestroy();
	}

}
