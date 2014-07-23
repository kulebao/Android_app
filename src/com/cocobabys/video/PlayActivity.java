package com.cocobabys.video;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cocobabys.R;
import com.cocobabys.utils.Utils;
import com.huamaitel.api.HMCallback;
import com.huamaitel.api.HMDefines;

/**
 * 视频主界面
 * 
 * @author admin
 * 
 */
public class PlayActivity extends Activity {
	private static final String TAG = "PlayActivity";

	// Records/Pictures/Logs related.
	public static final String FOLDER_NAME_RECORD = "records";
	public static final String FOLDER_NAME_CAPTURE = "pictures";
	public static final String FOLDER_NAME_LOG = "log";
	public static final int FILE_TYPE_RECORD = 1;
	public static final int FILE_TYPE_CAPTURE = 2;
	public static final int FILE_TYPE_LOG = 3;

	// 录像相关
	public static final int MSG_SHOW_RECORD_TIME = 11;
	public static final int MSG_STOP_RECORD = 12;
	private Button mbtn_record = null;
	private Button mbtn_capture = null;
	private Button mbtn_arming = null;
	private Button mbtn_disarming = null;
	private Button mbtn_opentalk = null;
	private Button mbtn_closetalk = null;
	private Button mbtn_openlisten = null;
	private Button mbtn_closelisten = null;
	private ImageView mivrecordDot = null;
	private TextView mtvrecordTime = null;

	private boolean mIfLogin = false; // If login...
	private boolean mIsPlaying = false; // Is playing video...
	private boolean mIsListening = false; // Is listening...
	private boolean mIsTalking = false; // Is talking...
	private boolean mIsRecording = false; // Is Recording...

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_activity);

		// 让屏幕保持不暗不关闭
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mbtn_record = (Button) findViewById(R.id.id_btn_record);
		mbtn_capture = (Button) findViewById(R.id.id_btn_capture);
		mbtn_arming = (Button) findViewById(R.id.id_btn_arming);
		mbtn_disarming = (Button) findViewById(R.id.id_btn_disarming);
		mbtn_opentalk = (Button) findViewById(R.id.id_btn_opentalk);
		mbtn_closetalk = (Button) findViewById(R.id.id_btn_closetalk);
		mbtn_openlisten = (Button) findViewById(R.id.id_btn_openlisten);
		mbtn_closelisten = (Button) findViewById(R.id.id_btn_closelisten);
		mtvrecordTime = (TextView) findViewById(R.id.record_time);
		mivrecordDot = (ImageView) findViewById(R.id.record_dot);
		mivrecordDot.setBackgroundResource(R.anim.record_anim);
		mivrecordDot.setImageDrawable(null);

		/**
		 * 打开录像
		 */
		mbtn_record.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String fileName = VideoApp.getJni().getNodeName(
						VideoApp.curNodeHandle);
				String path = getFilePath(FILE_TYPE_RECORD, fileName);
				if (mIsRecording) {
					VideoApp.getJni().stopLocalRecord(VideoApp.mRecordHandle);
					mIsRecording = false;
					mUIHandler.sendEmptyMessage(MSG_STOP_RECORD);
					Toast.makeText(getApplicationContext(),
							"停止录像，视频存放在：" + path, Toast.LENGTH_SHORT).show();
				} else {
					VideoApp.mCapturePath = path;
					VideoApp.mRecordHandle = VideoApp.getJni()
							.startLocalRecord(VideoApp.mUserId, path);

					if (VideoApp.mRecordHandle == 0) {
						Log.e(TAG, "start local record fail.");
					} else {
						Log.i(TAG, "start local record success.");
						showRecordAnim(true);
						Toast.makeText(getApplicationContext(), "开始录像",
								Toast.LENGTH_SHORT).show();
						showRecordTime();
						mIsRecording = true;
					}
				}
			}

			/*
			 * Show record time text
			 */
			private void showRecordTime() {
				new Thread() {
					@Override
					public void run() {
						super.run();
						while (mIsRecording) {
							mUIHandler.sendEmptyMessage(MSG_SHOW_RECORD_TIME);
							try {
								sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}.start();
			}
		});

		/**
		 * 拍照
		 */
		mbtn_capture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					takePic();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			private void takePic() {
				String fileName = VideoApp.getJni().getNodeName(
						VideoApp.curNodeHandle);
				String path = getFilePath(FILE_TYPE_CAPTURE, fileName);
				VideoApp.mCapturePath = path;

				byte data[] = VideoApp.getJni().localCapture(VideoApp.mUserId);
				if (null == data) {
					Toast.makeText(getApplicationContext(), "拍照失败",
							Toast.LENGTH_SHORT).show();
				} else {
					boolean res = saveCapturedPic(data, VideoApp.mCapturePath);
					if (res) {
						Utils.galleryAddPic(Uri.fromFile(new File(path)));
						Log.i(TAG, "Local capture success." + "拍照成功！图片存放在："
								+ path);
						Utils.makeToast(PlayActivity.this, "拍照成功，照片已保存到图库");

					} else {
						Log.e(TAG, "Local capture fail.");
					}
				}
			}
		});

		/**
		 * 打开听
		 */
		mbtn_openlisten.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startListen();
				mbtn_openlisten.setEnabled(false);
				mbtn_closelisten.setEnabled(true);

			}
		});

		/**
		 * 关闭听
		 */
		mbtn_closelisten.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopListen();

				mbtn_closelisten.setEnabled(false);
				mbtn_openlisten.setEnabled(true);
			}
		});

		/**
		 * 打开说
		 */
		mbtn_opentalk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startTalk();

				mbtn_opentalk.setEnabled(false);
				mbtn_closetalk.setEnabled(true);
			}
		});

		/**
		 * 关闭说
		 */
		mbtn_closetalk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopTalk();
				mbtn_opentalk.setEnabled(true);
				mbtn_closetalk.setEnabled(false);
			}
		});

		/**
		 * 布防
		 */
		mbtn_arming.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				VideoApp.getJni().arming(VideoApp.mUserId, 1, "");
				Toast.makeText(getApplicationContext(), "布防成功", 0).show();
				mbtn_arming.setEnabled(false);
				mbtn_disarming.setEnabled(true);

			}
		});

		/**
		 * 撤防
		 */
		mbtn_disarming.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				VideoApp.getJni().disarming(VideoApp.mUserId, 1, "");
				Toast.makeText(getApplicationContext(), "撤防成功", 0).show();
				mbtn_disarming.setEnabled(false);
				mbtn_arming.setEnabled(true);

			}
		});

		// 登录 & 打开视频
		openVideo();
	}

	private void startListen() {
		new Thread() {
			public void run() {
				HMDefines.OpenAudioParam param = new HMDefines.OpenAudioParam();
				param.channel = 0;
				HMDefines.OpenAudioRes res = new HMDefines.OpenAudioRes();

				VideoApp.mTalkHandle = VideoApp.getJni().startAudio(
						VideoApp.mUserId, param, res);
				if (VideoApp.mTalkHandle == 0) {
					mIsListening = true;
				}
				if (VideoApp.mTalkHandle == -1) {
					Log.e(TAG, "抱歉，该设备不支持音频功能");
				}
			}
		}.start();
	}

	private void stopListen() {
		int ret = VideoApp.getJni().stopAudio(VideoApp.mTalkHandle);
		if (ret != 0) {
			Log.e(TAG, "close the audio fail");
		} else {
			Log.i(TAG, "close the audio success");
		}
		mIsListening = false;
	}

	private void startTalk() {
		new Thread() {
			public void run() {
				HMDefines.OpenTalkParam param = new HMDefines.OpenTalkParam();
				param.channel = 0;
				param.audioEncode = VideoApp.mChannelCapacity[0].audioCodeType;
				param.sample = VideoApp.mChannelCapacity[0].audioSample;
				param.audioChannel = VideoApp.mChannelCapacity[0].audioChannel;

				VideoApp.mTalkHandle = VideoApp.getJni().startTalk(
						VideoApp.mUserId, param);
				if (VideoApp.mTalkHandle == 0) {
					mIsTalking = true;
				}
			}
		}.start();
	}

	private void stopTalk() {
		VideoApp.getJni().stopTalk(VideoApp.mTalkHandle);
		mIsTalking = false;
	}

	private void openVideo() {
		new Thread() {
			@Override
			public void run() {
				super.run();

				if (VideoApp.mIsUserLogin) {
					// 从互联网登录
					int nodeId = getIntent().getIntExtra("nodeId", 0);
					if (nodeId == 0) {
						return;
					}

					// Step 1: Login the device.
					VideoApp.mUserId = VideoApp.getJni().loginEx(nodeId);
					if (VideoApp.mUserId == 0) {
						return;
					}
				} else {
					// 从局域网登录（本地设备）
					String sip = getIntent().getStringExtra("ip");
					String sport = getIntent().getStringExtra("port");
					String suser = getIntent().getStringExtra("user");
					String spass = getIntent().getStringExtra("pass");
					String sSN = getIntent().getStringExtra("sn");
					if (sip != null && sport != null && sSN != null
							&& suser != null) {
						// Step 1: Login the device.
						VideoApp.mUserId = VideoApp.getJni().login(sip,
								Short.parseShort(sport), sSN, suser, spass);
						if (VideoApp.mUserId == 0) {
							return;
						}
					}
				}

				mIfLogin = true;

				// Step 2: Get device information.
				VideoApp.mDeviceInfo = VideoApp.getJni().getDeviceInfo(
						VideoApp.mUserId);
				VideoApp.mChannelCapacity = VideoApp.getJni()
						.getChannelCapacity(VideoApp.mUserId);
				if (VideoApp.mDeviceInfo == null) {
					return;
				}

				// Step 3: Open video
				HMDefines.OpenVideoParam param = new HMDefines.OpenVideoParam();
				param.channel = 0;
				param.codeStream = HMDefines.CodeStream.MAIN_STREAM;
				param.videoType = HMDefines.VideoStream.REAL_STREAM;
				HMDefines.OpenVideoRes res = new HMDefines.OpenVideoRes();

				// Step 4: Start video
				int ret = VideoApp.getJni().startVideo(VideoApp.mUserId, param,
						res);
				if (ret != HMDefines.HMEC_OK) {
					return;
				}
				if (VideoApp.mVideoHandle == 0) {
					return;
				}

				mIsPlaying = true;
			}
		}.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		exitPlayActivity();
	}

	private void exitPlayActivity() {
		new Thread() {
			@Override
			public void run() {
				super.run();

				if (mIsPlaying) {
					VideoApp.getJni().stopVideo(VideoApp.mVideoHandle);
				}

				if (mIsRecording) {
					VideoApp.getJni().stopLocalRecord(VideoApp.mRecordHandle);
				}

				if (mIsTalking) {
					VideoApp.getJni().stopTalk(VideoApp.mTalkHandle);
				}

				if (mIsListening) {
					VideoApp.getJni().stopAudio(VideoApp.mAudioHandle);
				}

				if (mIfLogin) {
					VideoApp.getJni().logout(VideoApp.mUserId);
				}
			}
		}.start();
	}

	/**
	 * 显示录像时间
	 */
	public Handler mUIHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SHOW_RECORD_TIME:
				int time = VideoApp.getJni().getLocalRecordTime(
						VideoApp.mRecordHandle);
				String timString = Duration2Time(time);
				mtvrecordTime.setText(timString);
				break;

			case MSG_STOP_RECORD:
				int result = VideoApp.getJni()
						.stopLocalRecord(VideoApp.mUserId);
				if (result != 0) {
					Log.e(TAG, "close local record fail.");
				} else {
					Log.i(TAG, "close local record success.");
				}

				mIsRecording = false;
				showRecordAnim(false);
				break;
			}
		}

	};

	/************************************************* 工具方法 *******************************************************/
	/*
	 * 将byte的数据保存为为Jpg、Png等图片格式。
	 */
	public static boolean saveCapturedPic(byte data[], String path) {
		Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
		FileOutputStream out = null;

		if (null == bmp) {
			Log.e(TAG, "bitmap is null.");
			return false;
		}

		try {
			out = new FileOutputStream(path);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return false;
		}

		bmp.compress(CompressFormat.PNG, 80, out);

		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * 时间格式转换
	 * 
	 * @param duration
	 * @return
	 */
	public static String Duration2Time(int duration) {
		String time = "";
		long ms = duration * 1000;

		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
		time = formatter.format(ms);

		return time;
	}

	/**
	 * 是否开始时间动画
	 * 
	 * @param isStart
	 */
	private void showRecordAnim(boolean isStart) {
		AnimationDrawable animator = (AnimationDrawable) mivrecordDot
				.getBackground();

		if (isStart) {
			mtvrecordTime.setVisibility(View.VISIBLE);
			mivrecordDot.setVisibility(View.VISIBLE);
			animator.start();
		} else {
			mtvrecordTime.setVisibility(View.GONE);
			mivrecordDot.setVisibility(View.GONE);
			animator.stop();
		}
	}

	/*
	 * Generate the path to save video/picture/log.
	 */
	public static String getFilePath(int fileType, String fileName) {
		String path = "";
		String sdPath = "";

		// Get the path of SD card.
		if (Utils.isSdcardExisting()) {
			sdPath = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
		} else {
			return path;
		}
		// Generate the path of Android client.
		path = sdPath + File.separator + "HMSDKDemo";
		switch (fileType) {
		case FILE_TYPE_RECORD:
			path += File.separator + FOLDER_NAME_RECORD;
			break;

		case FILE_TYPE_CAPTURE:
			path = Utils.getVideoPicPath(fileName);
			break;

		case FILE_TYPE_LOG:
			path += File.separator + FOLDER_NAME_LOG;
			break;

		default:
			break;
		}

		// Make sure the path is exist.
		File f = new File(path);
		if (!f.exists()) {
			f.mkdirs();
		}

		// Generate the file name.
		SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
		Date curDate = new Date(System.currentTimeMillis());
		String localTime = formatter.format(curDate);
		String filename = fileName + localTime;

		// Attach the extension name.
		switch (fileType) {
		case FILE_TYPE_RECORD:
			path += File.separator + filename + ".hmv";
			break;

		case FILE_TYPE_CAPTURE:
			path += File.separator + filename + ".png";
			break;

		case FILE_TYPE_LOG:
			path += File.separator + filename + ".txt";
			break;

		default:
			break;
		}

		return path;
	}

}

class PlayView extends GLSurfaceView {
	private PlayRenderer playRenderer;
	private boolean isFirstIn = true;

	public PlayView(Context context, AttributeSet attrs) {
		super(context);

		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2);

		// Set the Renderer for drawing on the GLSurfaceView
		playRenderer = new PlayRenderer();
		setRenderer(playRenderer);

		// Set render mode.
		setRenderMode(RENDERMODE_WHEN_DIRTY);

		// Register the OpenGL render callback.
		VideoApp.getJni().setRenderCallback(new HMCallback.RenderCallback() {
			@Override
			public void onRequest() {
				requestRender(); // Force to render if video data comes.
			}
		});
	}

	// 这个接口定义了在一个OpenGL的GLSurfaceView中绘制图形所需要的方法。
	class PlayRenderer implements GLSurfaceView.Renderer {

		// 设置OpenGL的环境变量，或是初始化OpenGL的图形物体。
		public void onSurfaceChanged(GL10 gl, int w, int h) {
			VideoApp.getJni().gLResize(w, h);
		}

		// 这个方法主要完成绘制图形的操作。
		public void onDrawFrame(GL10 gl) {
			if (isFirstIn) {
				isFirstIn = false;
				return;
			}

			VideoApp.getJni().gLRender();
		}

		// 主要用来对GLSurfaceView容器的变化进行响应。
		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			VideoApp.getJni().gLInit();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		super.surfaceDestroyed(holder);
		VideoApp.getJni().gLUninit();
	}
}
