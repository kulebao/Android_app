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
import android.util.DisplayMetrics;
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
import com.cocobabys.activities.MyApplication;

public class RecordButton extends Button {

	private static final int MIN_INTERVAL_TIME = 1000;// 1s
	private static final int MAX_INTERVAL_TIME = 1000 * 60;// 60s

	private static final int DIALOG_WIDTH = 200;
	private static final int DIALOG_HEIGHT = 200;

	private Context context;
	private State state = State.STATE_INIT;

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

	private long startTime;

	private Dialog recordIndicator;

	private static int[] res = { R.drawable.mic_1, R.drawable.mic_2, R.drawable.mic_3, R.drawable.mic_4,
			R.drawable.mic_5 };

	private ImageView view;

	private MediaRecorder recorder;

	private ObtainDecibelThread thread;

	private Handler volumeHandler;

	protected Rect rect;

	private void init() {
		volumeHandler = new ShowVolumeHandler() {

			@Override
			public void handleMessage(Message msg) {
				// 超时自动停止录音，并认为录音成功
				if (msg.what == -1) {
					handleFinishRecord();
					return;
				}
				if (view != null) {
					view.setImageResource(res[msg.what]);
				}
			}

		};

		setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();

				switch (action) {
				case MotionEvent.ACTION_DOWN:
					finishedListener.onActionDown();
					state = State.STATE_INIT;
					rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
					Log.d("DDD", "ACTION_DOWN fanwei :" + rect.toString());

					try {
						initDialogAndStartRecord();
					} catch (Exception e) {
						e.printStackTrace();
					}
					RecordButton.this.setText("松开 结束");
					break;
				case MotionEvent.ACTION_UP:
					Log.d("DDD", "ACTION_UP fanwei :" + rect.toString());
					if (state == State.STATE_INIT) {
						handleFinishRecord();
						state = State.STATE_FINISHED;
					}
					break;
				case MotionEvent.ACTION_MOVE:// 当手指移动到view外面，会cancel
					if (state == State.STATE_INIT
							&& (!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY()))) {
						cancelRecord();
						RecordButton.this.setText("按住 录音");
						state = State.STATE_CANCELED;
					}
					break;
				default:
					break;
				}

				return true;
			}

		});
	}

	private void handleFinishRecord() {
		finishRecord();
		RecordButton.this.setText("按住 录音");
	}

	private void initDialogAndStartRecord() {
		startTime = System.currentTimeMillis();
		recordIndicator = new Dialog(getContext(), R.style.like_toast_dialog_style);
		view = new ImageView(getContext());
		view.setImageResource(R.drawable.mic_2);
		recordIndicator.setContentView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		recordIndicator.setOnDismissListener(onDismiss);
		LayoutParams lp = recordIndicator.getWindow().getAttributes();
		lp.gravity = Gravity.CENTER;

		setDialogSize(lp);
		startRecording();
		recordIndicator.show();
	}

	private void setDialogSize(LayoutParams lp) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = MyApplication.getInstance().getResources().getDisplayMetrics();

		int width = (int) (DIALOG_WIDTH * dm.density);
		int height = (int) (DIALOG_HEIGHT * dm.density);

		lp.width = width;
		lp.height = height;
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

			finishedListener.onCanceledRecord(mFileName);
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

				long intervalTime = System.currentTimeMillis() - startTime;
				if (intervalTime > MAX_INTERVAL_TIME) {
					volumeHandler.sendEmptyMessage(-1);
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
					else if (f < 44)
						volumeHandler.sendEmptyMessage(3);
					else
						volumeHandler.sendEmptyMessage(4);

				}

			}
		}
	}

	private static enum State {
		STATE_INIT, STATE_FINISHED, STATE_CANCELED
	}

	private OnDismissListener onDismiss = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialog) {
			stopRecording();
		}
	};

	private static class ShowVolumeHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// view.setImageResource(res[msg.what]);
		}
	};

	public interface OnFinishedRecordListener {
		public void onActionDown();

		public void onFinishedRecord(String audioPath);

		public void onCanceledRecord(String audioPath);
	}

}
