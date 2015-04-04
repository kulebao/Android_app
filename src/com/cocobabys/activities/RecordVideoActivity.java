package com.cocobabys.activities;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.utils.Utils;
import com.skd.androidrecording.video.AdaptiveSurfaceView;
import com.skd.androidrecording.video.CameraHelper;
import com.skd.androidrecording.video.VideoRecordingHandler;
import com.skd.androidrecording.video.VideoRecordingManager;

public class RecordVideoActivity extends Activity {
	protected static final int DEFAULT_W = 800;

	protected static final int STEP_UP = 0;
	protected static final int START_RECORD = 100;
	protected static final int MAX_TIME = 30;
	private ProgressBar progress;
	private Thread thread;
	private Handler handler;
	private int currentProgress = 0;
	private String filename;
	private TextView timerView;
	// videoSize为null，recordingManager会自动使用800*480分辨率
	private Size videoSize = null;
	private VideoRecordingManager recordingManager;

	boolean isRecording = false;

	private VideoRecordingHandler recordingHandler = new VideoRecordingHandler() {
		@Override
		public boolean onPrepareRecording() {
			return false;
		}

		@Override
		public Size getVideoSize() {
			return videoSize;
		}

		@Override
		public int getDisplayRotation() {
			return RecordVideoActivity.this.getWindowManager()
					.getDefaultDisplay().getRotation();
		}

		@Override
		public void onSurfaceCreated() {
		}
	};

	private void startRecording() {
		Size fitSize = getFitSize();

		if (recordingManager.startRecording(filename, fitSize)) {
			isRecording = true;
			runProgressTask();
			return;
		}
		Utils.makeToast(this, "录像失败!");
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 选择支持半透明模式,在有surfaceview的activity中使用。
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.video);

		init();

		// handler.postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// handler.sendEmptyMessage(START_RECORD);
		// }
		// }, 500);
	}

	private void runProgressTask() {
		thread = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					while (true) {
						handler.sendEmptyMessage(STEP_UP);
						TimeUnit.SECONDS.sleep(1);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}

	private void init() {
		AdaptiveSurfaceView surfaceview = (AdaptiveSurfaceView) this
				.findViewById(R.id.surfaceview);
		progress = (ProgressBar) this.findViewById(R.id.progress);
		progress.setMax(MAX_TIME);

		timerView = (TextView) this.findViewById(R.id.timer);

		initHandler();
		createFile();

		Log.d("VideoRecordingManager", "init begain");
		recordingManager = new VideoRecordingManager(surfaceview,
				recordingHandler);
		Log.d("VideoRecordingManager", "init over");
	}

	// 有宽度为800的配置就用，否则就用800*480，简单判断一下
	private Size getFitSize() {
		List<Size> sizes = CameraHelper
				.getCameraSupportedVideoSizes(recordingManager
						.getCameraManager().getCamera());

		for (Size size : sizes) {
			if (size.width == DEFAULT_W) {
				return size;
			}
		}

		for (Size size : sizes) {
			if (size.width == 640) {
				return size;
			}
		}

		for (Size size : sizes) {
			if (size.width == 480) {
				return size;
			}
		}

		for (Size size : sizes) {
			if (size.width == 320) {
				return size;
			}
		}

		return recordingManager.getCameraManager().getCamera().new Size(800,
				480);
	}

	// 简单判断是否正在录像
	private boolean isRecording() {
		return isRecording;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isRecording()) {
			// 如果切换到后台时正在录像，则直接保存录像并跳转到预览界面
			finishRecord(null);
		}
	}

	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case STEP_UP:
					progress.setProgress(++currentProgress);
					if (currentProgress == MAX_TIME) {
						finishRecord(null);
						return;
					}
					setTime();
					break;
				case START_RECORD:
					startRecording();
					break;
				default:
					break;
				}
			}

		};
	}

	private void setTime() {
		if (currentProgress < 10) {
			timerView.setText("0" + currentProgress + "/" + MAX_TIME);
		} else {
			timerView.setText(currentProgress + "/" + MAX_TIME);
		}
	}

	private void stopRecord() {
		if (recordingManager != null) {
			// 停止录制
			recordingManager.stopRecording();
			recordingManager.dispose();
			recordingHandler = null;
		}

		stopThread();
	}

	private void createFile() {
		try {
			filename = Utils.getDefaultCameraDir() + System.currentTimeMillis()
					+ Utils.DEFAULT_VIDEO_ENDS;
			new File(filename).createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void finishRecord(View view) {
		isRecording = false;
		stopRecord();
		Intent intent = new Intent();
		intent.putExtra(ConstantValue.RECORD_FILE_NAME, filename);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		cancelRecord(null);
	}

	public void record(View view) {
		startRecording();
		findViewById(R.id.finishRecord).setVisibility(View.VISIBLE);
		findViewById(R.id.cancelRecord).setVisibility(View.VISIBLE);
		progress.setVisibility(View.VISIBLE);
		findViewById(R.id.record).setVisibility(View.GONE);
	}

	public void cancelRecord(View view) {
		isRecording = false;
		stopRecord();
		new File(filename).delete();
		finish();
	}

	private void stopThread() {
		if (thread != null) {
			thread.interrupt();
		}
	}
}