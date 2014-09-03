package com.cocobabys.video;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.Future;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.cocobabys.threadpool.MyThreadPoolMgr;
import com.cocobabys.utils.Utils;
import com.huamaitel.api.HMCallback;
import com.huamaitel.api.HMCallback.NetworkCallback;
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
	static Handler handler;
	static boolean bStartVideo = false;

	private static final int PLAY_SUCCESS = 0;
	private static final int PLAY_FAILED = 1;
	private static final int PLAY_LOGIN_FAILED = 2;
	private static final int PLAY_GET_DEVICE_FAILED = 3;

	private static final int NETWORK_ERROR = 4;

	private static final int GET_DEVICE_INFO = 10;
	private static final int PLAY_VIDEO = 20;
	private static final int OPEN_VIDEO_DONE = 30;
	static final int DRAW_FRAME = 40;

	private static final int EVT_START_LISTEN_SUCCESS = 50;
	private static final int EVT_START_LISTEN_FAILED = 60;
	private static final int EVT_CLOSE_LISTEN_SUCCESS = 70;
	private static final int EVT_CLOSE_LISTEN_FAILED = 80;

	private static final int EVT_TAKE_PIC_SUCCESS = 90;
	private static final int EVT_TAKE_PIC_FAILED = 100;

	private static final String LOGIN_DEVICE = "登录设备...";
	protected static final String GET_DEVICE = "获取设备信息...";
	protected static final String GET_VIDEO_INFO = "获取图像信息...";
	protected static final String BUFFERING = "缓冲中...";
	private static final int MAX_PROGRESS = 100;

	private ProgressDialog mProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_activity);

		// 让屏幕保持不暗不关闭
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		initHandler();

		initView();

		setRecordBtn();

		setTakePhotoBtnEx();

		setOpenListenBtn();

		setCloseListenBtn();

		setOpenSpeakBtn();

		setCloseSpeakBtn();

		setOpenGuardBtn();

		setCloseGuardBtn();

		showProgressDlg();

		bStartVideo = false;
		// 登录 & 打开视频
		openVideo();

	}

	private void showProgressDlg() {
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle(LOGIN_DEVICE);
		mProgressDialog.setProgress(10);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setMax(MAX_PROGRESS);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
				Utils.getResString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (openVideoFuture != null) {
							openVideoFuture.cancel(true);
						}
						finish();
					}
				});

		mProgressDialog.show();
	}

	private void setCloseGuardBtn() {
		/**
		 * 撤防
		 */
		mbtn_disarming.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				VideoApp.getJni().disarming(VideoApp.mUserId, 1, "");
				Toast.makeText(getApplicationContext(), "撤防成功",
						Toast.LENGTH_SHORT).show();
				mbtn_disarming.setEnabled(false);
				mbtn_arming.setEnabled(true);

			}
		});
	}

	private void setOpenGuardBtn() {
		/**
		 * 布防
		 */
		mbtn_arming.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				VideoApp.getJni().arming(VideoApp.mUserId, 1, "");
				Toast.makeText(getApplicationContext(), "布防成功",
						Toast.LENGTH_SHORT).show();
				mbtn_arming.setEnabled(false);
				mbtn_disarming.setEnabled(true);

			}
		});
	}

	private void setCloseSpeakBtn() {
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
	}

	private void setOpenSpeakBtn() {
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
	}

	private void setCloseListenBtn() {
		/**
		 * 关闭听
		 */
		mbtn_closelisten.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 如果没有正在播放音频，就不能关闭，否则死机。原因，handle无效，或者handle已经关闭过了，再次关闭
				if (mIsListening) {
					mbtn_closelisten.setEnabled(false);
					mbtn_openlisten.setEnabled(true);
					stopListen();
				}

				handler.sendEmptyMessage(EVT_CLOSE_LISTEN_SUCCESS);
			}
		});
	}

	private void setOpenListenBtn() {
		/**
		 * 打开听
		 */
		mbtn_openlisten.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mbtn_openlisten.setEnabled(false);
				mbtn_closelisten.setEnabled(true);
				startListen();
			}
		});
	}

	private void setTakePhotoBtnEx() {
		/**
		 * 拍照
		 */
		mbtn_capture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mbtn_capture.setVisibility(View.GONE);
				MyThreadPoolMgr.getGenericService().submit(new Runnable() {

					@Override
					public void run() {
						takePicEx();
					}
				});
			}

		});
	}

	private void takePicEx() {
		Log.d("EEE", "mbtn_capture takePic");
		int event = EVT_TAKE_PIC_FAILED;
		try {
			String fileName = VideoApp.getJni().getNodeName(
					VideoApp.curNodeHandle);
			String path = getFilePath(FILE_TYPE_CAPTURE, fileName);
			VideoApp.mCapturePath = path;

			byte data[] = VideoApp.getJni().localCapture(VideoApp.mUserId);
			if (null != data) {
				boolean res = saveCapturedPic(data, VideoApp.mCapturePath);
				if (res) {
					Utils.galleryAddPic(Uri.fromFile(new File(path)));
					event = EVT_TAKE_PIC_SUCCESS;
					Log.i(TAG, "Local capture success." + "拍照成功！图片存放在：" + path);
				} else {
					Log.e(TAG, "Local capture fail.");
				}
			}
		} finally {
			handler.sendEmptyMessage(event);
		}
	}

	private void setTakePhotoBtn() {
		/**
		 * 拍照
		 */
		mbtn_capture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					if (!mbtn_capture.isEnabled()) {
						Log.d("EEE",
								"mbtn_capture isEnabled false! do nothing!");
						return;
					}
					mbtn_capture.setEnabled(false);
					takePic();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					mbtn_capture.setEnabled(true);
				}
			}

		});
	}

	private void takePic() {
		Log.d("EEE", "mbtn_capture takePic");

		String fileName = VideoApp.getJni().getNodeName(VideoApp.curNodeHandle);
		String path = getFilePath(FILE_TYPE_CAPTURE, fileName);
		VideoApp.mCapturePath = path;

		byte data[] = VideoApp.getJni().localCapture(VideoApp.mUserId);
		if (null == data) {
			Utils.makeToast(PlayActivity.this, "拍照失败");
		} else {
			boolean res = saveCapturedPic(data, VideoApp.mCapturePath);
			if (res) {
				Utils.galleryAddPic(Uri.fromFile(new File(path)));
				Log.i(TAG, "Local capture success." + "拍照成功！图片存放在：" + path);
				Utils.makeToast(PlayActivity.this, "拍照成功，照片已保存到图库");

			} else {
				Log.e(TAG, "Local capture fail.");
			}
		}
	}

	private void setRecordBtn() {
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
	}

	private void initView() {
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
	}

	private void initHandler() {
		handler = new InnerHandler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (PlayActivity.this.isFinishing()) {
					return;
				}
				switch (msg.what) {
				case PLAY_SUCCESS:
					break;
				case PLAY_FAILED:
					Utils.makeToast(PlayActivity.this, "播放视频失败！");
					finish();
					break;
				case PLAY_LOGIN_FAILED:
					Utils.makeToast(PlayActivity.this, "注册视频服务失败！");
					finish();
					break;
				case PLAY_GET_DEVICE_FAILED:
					Utils.makeToast(PlayActivity.this, "获取视频设备失败！");
					finish();
					break;
				case GET_DEVICE_INFO:
					mProgressDialog.setTitle(GET_DEVICE);
					mProgressDialog.setProgress(30);
					break;
				case PLAY_VIDEO:
					mProgressDialog.setTitle(GET_VIDEO_INFO);
					mProgressDialog.setProgress(60);
					break;
				case OPEN_VIDEO_DONE:
					mProgressDialog.setTitle(BUFFERING);
					mProgressDialog.setProgress(99);
					break;
				case DRAW_FRAME:
					mProgressDialog.setProgress(100);
					mProgressDialog.dismiss();
					break;
				case NETWORK_ERROR:
					showErrorDialog(msg.arg1);
					break;

				case EVT_START_LISTEN_SUCCESS:
					Utils.makeToast(PlayActivity.this, "播放声音");
					break;
				case EVT_START_LISTEN_FAILED:
					mbtn_openlisten.setEnabled(true);
					Utils.makeToast(PlayActivity.this,
							"抱歉，打开声音失败，可能是设备不支持，请联系幼儿园处理，谢谢");
					break;
				case EVT_CLOSE_LISTEN_SUCCESS:
					Utils.makeToast(PlayActivity.this, "声音已关闭");
					break;
				case EVT_CLOSE_LISTEN_FAILED:
					showErrorDialog(msg.arg1);
					break;

				case EVT_TAKE_PIC_SUCCESS:
					mbtn_capture.setVisibility(View.VISIBLE);
					Utils.makeToast(PlayActivity.this, "拍照成功，照片已保存到图库");
					break;
				case EVT_TAKE_PIC_FAILED:
					mbtn_capture.setVisibility(View.VISIBLE);
					Utils.makeToast(PlayActivity.this, "拍照失败");
					break;

				default:
					break;
				}
			}

		};
	}

	private void startListen() {
		MyThreadPoolMgr.getGenericService().submit(new Runnable() {

			@Override
			public void run() {
				HMDefines.OpenAudioParam param = new HMDefines.OpenAudioParam();
				param.channel = 0;
				HMDefines.OpenAudioRes res = new HMDefines.OpenAudioRes();

				VideoApp.mAudioHandle = VideoApp.getJni().startAudio(
						VideoApp.mUserId, param, res);
				Log.d("VIDEO", "mAudioHandle =" + VideoApp.mAudioHandle);
				if (VideoApp.mAudioHandle > 0) {
					handler.sendEmptyMessage(EVT_START_LISTEN_SUCCESS);
					mIsListening = true;
				} else {
					handler.sendEmptyMessage(EVT_START_LISTEN_FAILED);
				}

			}
		});
	}

	private void stopListen() {
		int ret = VideoApp.getJni().stopAudio(VideoApp.mAudioHandle);
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

	private boolean loginToDevice() {
		if (VideoApp.mIsUserLogin) {
			// 从互联网登录
			int nodeId = getIntent().getIntExtra("nodeId", 0);
			Log.d("VIDEO", "first nodeId=" + nodeId);
			if (nodeId == 0) {
				return false;
			}

			// Step 1: Login the device.
			Log.d("VIDEO", "try to login");
			VideoApp.mUserId = VideoApp.getJni().loginEx(nodeId);
			Log.d("VIDEO", "after login mUserId=" + VideoApp.mUserId);
			// 原始代码这里只有0才返回，实测中会返回-1，如果返回-1继续向下执行，程序假死，且按键无反应
			if (VideoApp.mUserId == 0 || VideoApp.mUserId == -1) {
				handler.sendEmptyMessage(PLAY_LOGIN_FAILED);
				return false;
			}
		} else {
			// 从局域网登录（本地设备）
			String sip = getIntent().getStringExtra("ip");
			String sport = getIntent().getStringExtra("port");
			String suser = getIntent().getStringExtra("user");
			String spass = getIntent().getStringExtra("pass");
			String sSN = getIntent().getStringExtra("sn");
			if (sip != null && sport != null && sSN != null && suser != null) {
				// Step 1: Login the device.
				VideoApp.mUserId = VideoApp.getJni().login(sip,
						Short.parseShort(sport), sSN, suser, spass);
				if (VideoApp.mUserId == 0) {
					return false;
				}
			}
		}

		return true;
	}

	private boolean getDeviceInfo() {
		Log.d("VIDEO", "get device info");
		VideoApp.mDeviceInfo = VideoApp.getJni()
				.getDeviceInfo(VideoApp.mUserId);
		Log.d("VIDEO", "getChannelCapacity mDeviceInfo=" + VideoApp.mDeviceInfo);
		if (VideoApp.mDeviceInfo == null) {
			handler.sendEmptyMessage(PLAY_GET_DEVICE_FAILED);
			return false;
		}

		VideoApp.mChannelCapacity = VideoApp.getJni().getChannelCapacity(
				VideoApp.mUserId);

		return true;
	}

	private boolean playVideo() {
		Log.d("VIDEO", "OpenVideoParam");
		HMDefines.OpenVideoParam param = new HMDefines.OpenVideoParam();
		param.channel = 0;
		param.codeStream = HMDefines.CodeStream.MAIN_STREAM;
		param.videoType = HMDefines.VideoStream.REAL_STREAM;
		Log.d("VIDEO", "OpenVideoRes");
		HMDefines.OpenVideoRes res = new HMDefines.OpenVideoRes();

		Log.d("VIDEO", "startVideo");
		VideoApp.mVideoHandle = VideoApp.getJni().startVideo(VideoApp.mUserId,
				param, res);

		Log.d("VIDEO", "at the end mVideoHandle =" + VideoApp.mVideoHandle);
		if (VideoApp.mVideoHandle <= 0) {
			handler.sendEmptyMessage(PLAY_FAILED);
			return false;
		}

		VideoApp.getJni().setNetworkCallback(VideoApp.mUserId,
				new NetworkCallback() {
					@Override
					public void onNetwork(int errorCode) {
						handleNetworkCallBack(errorCode);
					}

				});
		return true;
	}

	private void handleNetworkCallBack(int errorCode) {
		if (errorCode != HMDefines.HMEC_OK) {
			Message message = Message.obtain();
			message.what = NETWORK_ERROR;
			message.arg1 = errorCode;
			handler.sendMessage(message);
		}
	}

	private void showErrorDialog(int errorCode) {
		// final AlertDialog dialog = new AlertDialog.Builder(PlayActivity.this)
		// .setPositiveButton(R.string.confirm,
		// new android.content.DialogInterface.OnClickListener() {
		// @Override
		// public void onClick(DialogInterface dialog,
		// int which) {
		// dialog.dismiss();
		// finish();
		// }
		// }).setCancelable(false)
		// .setTitle("网络错误，请重新尝试！ error=" + errorCode).create();
		// dialog.show();

		Utils.makeToast(this, "网络异常:errorCode=" + errorCode);
	}

	private void openVideo() {
		openVideoFuture = MyThreadPoolMgr.getGenericService().submit(
				new Runnable() {
					@Override
					public void run() {
						boolean result = loginToDevice();

						if (!result) {
							return;
						}

						mIfLogin = true;

						// Step 2: Get device information.
						handler.sendEmptyMessage(GET_DEVICE_INFO);
						result = getDeviceInfo();
						if (!result) {
							return;
						}

						// Step 3: Open video
						handler.sendEmptyMessage(PLAY_VIDEO);
						result = playVideo();
						if (!result) {
							return;
						}

						bStartVideo = true;
						handler.sendEmptyMessage(OPEN_VIDEO_DONE);
						mIsPlaying = true;
					}
				});
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (mIsListening) {
			startListen();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d("VIDEO", "stopAudio");
		if (mIsListening) {
			VideoApp.getJni().stopAudio(VideoApp.mAudioHandle);
		}
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this).setTitle("确定要退出视频播放吗？")
				.setIcon(android.R.drawable.ic_dialog_info)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						PlayActivity.this.finish();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mProgressDialog != null) {
			mProgressDialog.cancel();
		}
		exitPlayActivity();
	}

	private void exitPlayActivity() {
		MyThreadPoolMgr.getGenericService().submit(new Runnable() {

			@Override
			public void run() {
				Log.d("VIDEO", "stopVideo");
				if (mIsPlaying) {
					VideoApp.getJni().stopVideo(VideoApp.mVideoHandle);
				}

				Log.d("VIDEO", "stopLocalRecord");
				if (mIsRecording) {
					VideoApp.getJni().stopLocalRecord(VideoApp.mRecordHandle);
				}
				Log.d("VIDEO", "stopTalk");
				if (mIsTalking) {
					VideoApp.getJni().stopTalk(VideoApp.mTalkHandle);
				}

				// onstop里执行过了，这里不再执行,否则导致app崩溃
				// if (mIsListening) {
				// VideoApp.getJni().stopAudio(VideoApp.mAudioHandle);
				// }
				Log.d("VIDEO", "logout");
				if (mIfLogin) {
					VideoApp.getJni().logout(VideoApp.mUserId);
				}
			}
		});
	}

	/**
	 * 显示录像时间
	 */
	public Handler mUIHandler = new InnerHandler() {
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

	private Future<?> openVideoFuture;

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

	public static class InnerHandler extends Handler {

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
				Log.d("Renderer", "onRequest");
				requestRender(); // Force to render if video data
									// comes.
			}
		});
	}

	// 这个接口定义了在一个OpenGL的GLSurfaceView中绘制图形所需要的方法。
	private class PlayRenderer implements GLSurfaceView.Renderer {

		// 设置OpenGL的环境变量，或是初始化OpenGL的图形物体。
		public void onSurfaceChanged(GL10 gl, int w, int h) {
			Log.d("Renderer", "onSurfaceChanged");
			VideoApp.getJni().gLResize(w, h);
		}

		// 这个方法主要完成绘制图形的操作。
		public void onDrawFrame(GL10 gl) {
			if (isFirstIn) {
				// 在执行了getdeviceinfo后，这里会出现一帧回调，不知道为啥，忽略掉该帧
				isFirstIn = false;
				Log.d("VIDEO", "onDrawFrame");
				return;
			}

			if (PlayActivity.bStartVideo) {
				PlayActivity.bStartVideo = false;
				PlayActivity.handler.sendEmptyMessage(PlayActivity.DRAW_FRAME);
			}

			Log.d("Renderer", "onDrawFrame");
			VideoApp.getJni().gLRender();
		}

		// 主要用来对GLSurfaceView容器的变化进行响应。
		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			Log.d("Renderer", "onSurfaceCreated");
			VideoApp.getJni().gLInit();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		super.surfaceDestroyed(holder);
		Log.d("Renderer", "surfaceDestroyed");
		VideoApp.getJni().gLUninit();
	}

}