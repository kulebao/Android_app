package com.cocobabys.activities;

import java.io.File;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.httpclientmgr.HttpClientHelper.DownloadFileListener;
import com.cocobabys.threadpool.MyThreadPoolMgr;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

public class UpdateActivity extends UmengStatisticsActivity {
	private static final String TMP_APK_NAME = "cocobabys.apk";
	private String current_apk_url;
	private ProgressDialog mProgressDialog;
	private static final int MAX_PROGRESS = 100;
	private long totalSize = 0;
	private long currentSize = 0;
	private long currentProgress = 0;
	private String savepath;
	private Handler handler;

	private static final int ERROR = 100;
	private static final int COMPLETE = 101;
	private static final int DOWNLOADING = 102;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update);
		initHandler();
		initView();
		savepath = new File(Utils.getSDCardFileDir(Utils.APP_DIR_TMP),
				TMP_APK_NAME).getAbsolutePath();
	}

	private void initHandler() {
		handler = new MyHandler(this, null) {

			@Override
			public void handleMessage(Message msg) {
				if (UpdateActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case COMPLETE:
					installApk();
					mProgressDialog.dismiss();
					break;
				case ERROR:
					try {
						handleException((Exception) msg.obj);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case DOWNLOADING:
					handleDownloading();
					break;
				default:
					break;
				}
			}
		};
	}

	protected void handleDownloading() {
		// 可以不用考虑溢出的问题
		int now = (int) (currentSize * 100 / totalSize);

		if (now > 100) {
			now = 100;
		}

		if (now > currentProgress) {
			mProgressDialog.setProgress(now);
			currentProgress = now;
		}
	}

	private void initView() {
		initTextview();
		initBtn();
	}

	private void downloadApkByBrowser() {
		try {
			Log.d("DJC GET ", "url = " + current_apk_url);
			Uri uri = Uri.parse(current_apk_url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			UpdateActivity.this.finish();
		} catch (Exception e) {
			e.printStackTrace();
			Utils.makeToast(UpdateActivity.this, "更新失败，非法地址:" + current_apk_url);
		}
	}

	private void initBtn() {
		Button updateBtn = (Button) findViewById(R.id.updateBtn);
		updateBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String last_update_url = DataUtils
						.getProp(JSONConstant.LAST_UPDATE_URL);
				if (current_apk_url.equals(last_update_url)) {
					File file = new File(savepath);

					if (file.exists()
							&& file.length() == Long.valueOf(DataUtils
									.getProp(JSONConstant.FILE_SIZE))) {
						Log.d("DDD", "installApk");
						installApk();
					} else {
						downloadApkBySelf();
					}
				} else {
					Log.d("DDD", "downloadApkBySelf");
					downloadApkBySelf();
				}
			}

		});

		Button cancelUpdateBtn = (Button) findViewById(R.id.cancelUpdateBtn);
		cancelUpdateBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				UpdateActivity.this.finish();
			}
		});
	}

	private void downloadApkBySelf() {
		showProgressDlg();

		Log.d("EEE", "url =" + current_apk_url);
		Log.d("EEE", "savepath =" + savepath);
		MyThreadPoolMgr.getGenericService().submit(new Runnable() {

			@Override
			public void run() {
				HttpClientHelper.downloadFile(current_apk_url, savepath,
						new DownloadFileListener() {
							@Override
							public void onException(Exception e) {
								Message message = Message.obtain();
								message.obj = e;
								message.what = ERROR;
								handler.sendMessage(message);
							}

							@Override
							public void onDownloading(int size) {
								currentSize += size;
								handler.sendEmptyMessage(DOWNLOADING);
							}

							@Override
							public void onComplete() {
								DataUtils.saveProp(JSONConstant.LAST_UPDATE_URL,
										current_apk_url);
								handler.sendEmptyMessage(COMPLETE);
							}

							@Override
							public void onBegain(long contentLength) {
								totalSize = contentLength;
							}
						});
			}
		});

	}

	protected void handleException(Exception e) {
		mProgressDialog.dismiss();
		Utils.makeToast(this, "下载应用失败:" + e.toString());
	}

	private void installApk() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(savepath)),
				"application/vnd.android.package-archive");
		UpdateActivity.this.startActivity(intent);
		UpdateActivity.this.finish();
	}

	private void showProgressDlg() {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle("正在下载新版本应用...");
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setMax(MAX_PROGRESS);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setProgress(0);
		mProgressDialog.show();
	}

	// 在ChechUpdateMethod中保存到文件，这里直接从文件获取
	public void initTextview() {
		current_apk_url = DataUtils.getProp(JSONConstant.UPDATE_URL);

		String content = DataUtils.getProp(JSONConstant.UPDATE_CONTENT);
		content = content.replace("\\n", "\n");
		String versionName = DataUtils.getProp(JSONConstant.UPDATE_VERSION_NAME);
		long fileSize = Long.valueOf(DataUtils.getProp(JSONConstant.FILE_SIZE));

		TextView version = (TextView) findViewById(R.id.versionNameContent);
		version.setText(versionName);

		TextView size = (TextView) findViewById(R.id.sizecontent);
		size.setText((fileSize / 1024) + "k");

		TextView summarycontent = (TextView) findViewById(R.id.summarycontent);
		summarycontent.setText(content);

		// buffer.append("版本号 " + versionName + "\n");
		// buffer.append("版本大小 " + (fileSize / 1024) + "k" + "\n");
		// buffer.append("更新说明:\n");
		// buffer.append(content);
	}
}
