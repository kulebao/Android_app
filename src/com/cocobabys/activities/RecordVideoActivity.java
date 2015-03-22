package com.cocobabys.activities;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cocobabys.R;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.utils.Utils;

public class RecordVideoActivity extends Activity implements
		SurfaceHolder.Callback {
	protected static final int STEP_UP = 0;
	protected static final int MAX_TIME = 30;
	private MediaRecorder mediarecorder;// 录制视频的类
	private SurfaceView surfaceview;// 显示视频的控件
	// 用来显示视频的一个接口，我靠不用还不行，也就是说用mediarecorder录制视频还得给个界面看
	// 想偷偷录视频的同学可以考虑别的办法。。嗯需要实现这个接口的Callback接口
	private ProgressBar progress;
	private Thread thread;
	private Handler handler;
	private int currentProgress = 0;
	private String filename;
	private TextView timerView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 选择支持半透明模式,在有surfaceview的activity中使用。
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		setContentView(R.layout.video);

		init();
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
		progress = (ProgressBar) this.findViewById(R.id.progress);
		timerView = (TextView) this.findViewById(R.id.timer);

		initSurface();
		initHandler();
		createFile();
	}

	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case STEP_UP:
					progress.setProgress(currentProgress++);
					if (currentProgress == MAX_TIME) {
						finishRecord(null);
						return;
					}
					setTime();
					break;

				default:
					break;
				}
			}

		};
	}

	private void setTime() {
		if (currentProgress < 10) {
			timerView.setText("00:0" + currentProgress);
		} else {
			timerView.setText("00:" + currentProgress);
		}
	}

	private void initSurface() {
		surfaceview = (SurfaceView) this.findViewById(R.id.surfaceview);
		SurfaceHolder holder = surfaceview.getHolder();// 取得holder
		holder.addCallback(this); // holder加入回调接口
		// setType必须设置，要不出错.
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	private void stopRecord() {
		if (mediarecorder != null) {
			// 停止录制
			mediarecorder.stop();
			// 释放资源
			mediarecorder.release();
			mediarecorder = null;
		}
	}

	private void doRecord(SurfaceHolder holder) {
		mediarecorder = new MediaRecorder();// 创建mediarecorder对象.
		mediarecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

		// 设置录制视频源为Camera(相机)
		mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		// 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
		mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mediarecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		// 设置录制的视频编码h263 h264
		mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
		// 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
		mediarecorder.setVideoSize(800, 480);
		// 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
		mediarecorder.setVideoFrameRate(50);
		mediarecorder.setPreviewDisplay(holder.getSurface());

		mediarecorder.setOutputFile(filename);
		try {
			// 准备录制
			Log.d("", "DDDD prepare start");
			mediarecorder.prepare();
			// 开始录制
			mediarecorder.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			doRecord(holder);
			runProgressTask();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// surfaceDestroyed的时候同时对象设置为null
		surfaceview = null;
		mediarecorder = null;
	}

	public void finishRecord(View view) {
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

	public void cancelRecord(View view) {
		stopRecord();
		new File(filename).delete();
		if (thread != null) {
			thread.interrupt();
		}
		finish();
	}
}