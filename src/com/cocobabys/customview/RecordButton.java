package com.cocobabys.customview;

import java.io.File;
import java.io.IOException;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Rect;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.cocobabys.R;

public class RecordButton extends Button {

	private Context context;

	public RecordButton(Context context) {
		super(context);
		this.context = context;
		init();
	}

	public RecordButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		init();
	}

	public RecordButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	public void setSavePath(String path) {
		mFileName = path;
	}

	public void setOnFinishedRecordListener(OnFinishedRecordListener listener) {
		finishedListener = listener;
	}

	private String mFileName = null;

	private OnFinishedRecordListener finishedListener = new OnFinishedRecordListener() {
		@Override
		public void onFinishedRecord(String audioPath) {
		}

		@Override
		public void onCanceledRecord(String audioPath) {
		}

		@Override
		public void onActionDown() {
		}
	};

	private static final int MIN_INTERVAL_TIME = 1000;// 1s
	private long startTime;

	private Dialog recordIndicator;

	private static int[] res = { R.drawable.mic_2, R.drawable.mic_3,
			R.drawable.mic_4, R.drawable.mic_5 };

	private static ImageView view;

	private MediaRecorder recorder;

	private ObtainDecibelThread thread;

	private Handler volumeHandler;

	protected Rect rect;

	private void init() {
		volumeHandler = new ShowVolumeHandler();
		setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();

				switch (action) {
				case MotionEvent.ACTION_DOWN:
					finishedListener.onActionDown();
					
					rect = new Rect(v.getLeft(), v.getTop(), v.getRight(),
							v.getBottom());
					Log.d("DDD", "ACTION_DOWN fanwei :" + rect.toString());

					initDialogAndStartRecord();
					RecordButton.this.setText("松开 结束");
					break;
				case MotionEvent.ACTION_UP:
					Log.d("DDD", "ACTION_UP fanwei :" + rect.toString());
					finishRecord();
					RecordButton.this.setText("按住 录音");
					break;
				case MotionEvent.ACTION_MOVE:// 当手指移动到view外面，会cancel
					if (!rect.contains(v.getLeft() + (int) event.getX(),
							v.getTop() + (int) event.getY())) {
						cancelRecord();
					}
					break;
				default:
					break;
				}

				return true;
			}
		});
	}

	private void initDialogAndStartRecord() {
		startTime = System.currentTimeMillis();
		recordIndicator = new Dialog(getContext(),
				R.style.like_toast_dialog_style);
		view = new ImageView(getContext());
		view.setImageResource(R.drawable.mic_2);
		recordIndicator.setContentView(view, new LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		recordIndicator.setOnDismissListener(onDismiss);
		LayoutParams lp = recordIndicator.getWindow().getAttributes();
		lp.gravity = Gravity.CENTER;

		startRecording();
		recordIndicator.show();
	}

	private void finishRecord() {
		stopRecording();
		recordIndicator.dismiss();

		long intervalTime = System.currentTimeMillis() - startTime;
		if (intervalTime < MIN_INTERVAL_TIME) {
			Toast.makeText(getContext(), "时间太短！", Toast.LENGTH_SHORT).show();
			File file = new File(mFileName);
			file.delete();
			return;
		}

		finishedListener.onFinishedRecord(mFileName);
	}

	private void cancelRecord() {
		if (recorder != null) {
			Log.d("DDD", "ACTION_MOVE cancelRecord");
			stopRecording();
			recordIndicator.dismiss();

			Toast.makeText(getContext(), "取消录音！", Toast.LENGTH_SHORT).show();
			File file = new File(mFileName);
			file.delete();

			finishedListener.onFinishedRecord(mFileName);
		}
	}

	private void startRecording() {
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOutputFile(mFileName);

		try {
			recorder.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}

		recorder.start();
		thread = new ObtainDecibelThread();
		thread.start();

	}

	private void stopRecording() {
		if (thread != null) {
			thread.exit();
			thread = null;
		}
		if (recorder != null) {
			recorder.stop();
			recorder.release();
			recorder = null;
		}
	}

	private class ObtainDecibelThread extends Thread {

		private volatile boolean running = true;

		public void exit() {
			running = false;
		}

		@Override
		public void run() {
			while (running) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (recorder == null || !running) {
					break;
				}
				int x = recorder.getMaxAmplitude();
				if (x != 0) {
					int f = (int) (10 * Math.log(x) / Math.log(10));
					if (f < 26)
						volumeHandler.sendEmptyMessage(0);
					else if (f < 32)
						volumeHandler.sendEmptyMessage(1);
					else if (f < 38)
						volumeHandler.sendEmptyMessage(2);
					else
						volumeHandler.sendEmptyMessage(3);

				}

			}
		}

	}

	private OnDismissListener onDismiss = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialog) {
			stopRecording();
		}
	};

	static class ShowVolumeHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			view.setImageResource(res[msg.what]);
		}
	};

	public interface OnFinishedRecordListener {
		public void onActionDown();

		public void onFinishedRecord(String audioPath);

		public void onCanceledRecord(String audioPath);
	}

}
