package com.cocobabys.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.cocobabys.R;
import com.cocobabys.adapter.GalleryAdapter;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.NoticeAction;
import com.cocobabys.customview.CustomGallery;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.SendExpJob;
import com.cocobabys.utils.ImageUtils;
import com.cocobabys.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SendExpActivity extends UmengStatisticsActivity {
	private GridView gridGallery;
	private Handler myhandler;
	private GalleryAdapter adapter;
	private ViewSwitcher viewSwitcher;
	private ImageLoader imageLoader;
	private Uri uri;
	private EditText exp_content;
	private MediaScannerConnection msc;
	private ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_exp);
		msc = new MediaScannerConnection(this,
				new MyMediaScannerConnectionClient());
		initImageLoader();
		initUI();
		initHander();
	}

	private void initDialog() {
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage(getResources().getString(R.string.sending));
	}

	private void initImageLoader() {
		imageLoader = ImageUtils.getImageLoader();
	}

	private void initHeader() {
		Button send = (Button) findViewById(R.id.rightBtn);
		send.setText(R.string.send);
		send.setVisibility(View.VISIBLE);
		send.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!checkContentValid()) {
					Utils.makeToast(SendExpActivity.this,
							R.string.invalid_exp_content);
					return;
				}
				runSendExpJob();
			}
		});

		Button cancel = (Button) findViewById(R.id.leftBtn);
		cancel.setText(R.string.cancel);
		cancel.setVisibility(View.VISIBLE);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SendExpActivity.this.finish();
			}
		});

	}

	protected boolean checkContentValid() {
		if (TextUtils.isEmpty(exp_content.getText().toString().trim())
				&& adapter.getCount() == 0) {
			return false;
		}
		return true;
	}

	private void initHander() {
		myhandler = new MyHandler(this, dialog) {
			@Override
			public void handleMessage(Message msg) {
				if (SendExpActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.POST_EXP_SUCCESS:
					handSendExpSuccess();
					break;
				case EventType.POST_EXP_FAIL:
					Utils.makeToast(SendExpActivity.this, R.string.send_fail);
					break;
				case EventType.UPLOAD_ICON_SUCCESS:
					dialog.setProgress(msg.arg1);
					break;
				default:
					break;
				}
			}

		};
	}

	private void handSendExpSuccess() {
		Utils.makeToast(SendExpActivity.this, R.string.send_success);
		clearAllContent();
	}

	private void initUI() {
		initHeader();
		initDialog();

		exp_content = (EditText) findViewById(R.id.exp_content);
		initGallery();
		initBtn();
	}

	private void initGallery() {
		gridGallery = (GridView) findViewById(R.id.gridGallery);
		gridGallery.setFastScrollEnabled(true);
		adapter = new GalleryAdapter(getApplicationContext(), imageLoader);
		adapter.setMultiplePick(false);
		gridGallery.setAdapter(adapter);

		gridGallery.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d("DDDDDD", "startToSlideGalleryActivity position="
						+ position);
				startToSlideGalleryActivity(position);
			}
		});

		viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
		viewSwitcher.setDisplayedChild(1);
	}

	private void runSendExpJob() {
		List<CustomGallery> list = adapter.getData();
		List<String> mediaPaths = new ArrayList<String>();
		if (!list.isEmpty()) {
			for (CustomGallery gallery : list) {
				mediaPaths.add(gallery.getSdcardPath());
			}
		}
		if (!mediaPaths.isEmpty()) {
			dialog.setMax(mediaPaths.size());
			dialog.setProgress(1);
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		} else {
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}
		dialog.show();

		SendExpJob expJob = new SendExpJob(myhandler, exp_content.getText()
				.toString(), mediaPaths);
		expJob.execute();
	}

	// 将拍照后保存的照片加入到图库,速度慢
	private void galleryAddPic() {
		Intent mediaScanIntent = new Intent(
				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		mediaScanIntent.setData(uri);
		this.sendBroadcast(mediaScanIntent);
	}

	// 将拍照后保存的照片加入到图库,速度快
	private void galleryAddPicExt() {
		msc.connect();
	}

	protected void startToSlideGalleryActivity(int position) {
		Intent intent = new Intent(NoticeAction.ACTION_GALLERY_CAN_DELETE);
		List<String> allSelectedPath = adapter.getAllSelectedPath();
		if (!allSelectedPath.isEmpty()) {
			intent.putExtra(NoticeAction.SELECTED_PATH,
					allSelectedPath.toArray(new String[allSelectedPath.size()]));
		}

		intent.putExtra(NoticeAction.EXP_TEXT, exp_content.getText().toString());
		intent.putExtra(NoticeAction.GALLERY_POSITION, position);
		startActivityForResult(intent, NoticeAction.SELECT_SLIDE_GALLERY);
	}

	private void initBtn() {
		Button camera = (Button) findViewById(R.id.camera);
		camera.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!Utils.isSdcardExisting()) {
					Toast.makeText(SendExpActivity.this, "未找到存储卡，无法保存图片！",
							Toast.LENGTH_LONG).show();
					return;
				}

				if (adapter.checkMaxIconSelected()) {
					return;
				}

				chooseIconFromCamera();
			}
		});

		Button gallery = (Button) findViewById(R.id.gallery);
		gallery.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(NoticeAction.ACTION_MULTIPLE_PICK);
				List<String> allSelectedPath = adapter.getAllSelectedPath();
				if (!allSelectedPath.isEmpty()) {
					i.putExtra(NoticeAction.SELECTED_PATH, allSelectedPath
							.toArray(new String[allSelectedPath.size()]));
				}
				startActivityForResult(i, NoticeAction.SELECT_GALLERY);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		switch (requestCode) {
		case NoticeAction.SELECT_CAMERA:
			handleTakePicSuccess();
			break;
		case NoticeAction.SELECT_GALLERY:
			String[] all_path = data.getStringArrayExtra(NoticeAction.ALL_PATH);
			Log.d("DJC", "path aaa =" + all_path[0]);
			changeView(all_path);
			break;
		case NoticeAction.SELECT_SLIDE_GALLERY:
			String[] changed_array = data
					.getStringArrayExtra(NoticeAction.PATH_AFTER_CHANGE);
			changeView(changed_array);
			break;
		default:
			break;
		}

	}

	private void clearAllContent() {
		exp_content.setText("");
		adapter.clearCache();
		adapter.clear();
	}

	private void handleTakePicSuccess() {
		// galleryAddPic();
		galleryAddPicExt();
		CustomGallery gallery = new CustomGallery();
		Log.d("DJC", "path bbb=" + uri.getPath());
		gallery.setSdcardPath(uri.getPath());
		gallery.setSeleted(true);
		viewSwitcher.setDisplayedChild(0);
		adapter.insert(gallery);
	}

	private void chooseIconFromCamera() {
		File file = getFile();
		uri = Uri.fromFile(file);
		Log.d("DJC", "path =" + uri.getPath());
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		startActivityForResult(cameraIntent, NoticeAction.SELECT_CAMERA);
	}

	private File getFile() {
		String path = Utils.getDefaultCameraDir();
		Utils.makeDirs(path);
		File file = new File(path, new Date().getTime() + ".jpg");
		return file;
	}

	private void changeView(String[] all_path) {
		ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();

		for (String string : all_path) {
			CustomGallery customGallery = new CustomGallery();
			customGallery.setSdcardPath(string);
			customGallery.setSeleted(true);
			dataT.add(customGallery);
		}

		viewSwitcher.setDisplayedChild(0);
		adapter.addAll(dataT);
	}

	@Override
	protected void onDestroy() {
		adapter.clearCache();
		super.onDestroy();
	}

	private class MyMediaScannerConnectionClient implements
			MediaScannerConnectionClient {

		@Override
		public void onMediaScannerConnected() {
			Log.d("DDD", "onMediaScannerConnected");
			msc.scanFile(uri.getPath(), "image/jpeg");
		}

		@Override
		public void onScanCompleted(String path, Uri uri) {
			Log.d("DDD", "onScanCompleted");
			msc.disconnect();
		}

	}
}
