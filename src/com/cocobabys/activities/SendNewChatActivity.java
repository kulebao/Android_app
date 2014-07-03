package com.cocobabys.activities;

import java.io.File;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cocobabys.R;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.customview.RecordButton;
import com.cocobabys.customview.RecordButton.OnFinishedRecordListener;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.InfoHelper;
import com.cocobabys.dbmgr.info.NewChatInfo;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.SendChatJob;
import com.cocobabys.jobs.UploadMediaFileIconJob;
import com.cocobabys.utils.Utils;

public class SendNewChatActivity extends UmengStatisticsActivity {
	private EditText chatContent;
	private MyHandler handler;
	private ProgressDialog dialog;
	private String childid;
	private RecordButton record_button;
	private String currentMediaPath = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.write_notice);
		childid = DataMgr.getInstance().getSelectedChild().getServer_id();
		initView();
		initHandler();
	}

	private void initView() {
		initDialog();
		initBtn();
	}

	public void closeKeyBoard() {
		View view = getWindow().peekDecorView();
		if (view != null) {
			InputMethodManager inputmanger = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	public void initBtn() {
		chatContent = (EditText) findViewById(R.id.edit_notice);
		TextView sendBtn = (TextView) findViewById(R.id.send);
		sendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isEmptyInput()) {
					Toast.makeText(SendNewChatActivity.this,
							R.string.pls_input_chat, Toast.LENGTH_SHORT).show();
					return;
				}
				runSendChatTask();
			}
		});

		TextView cancel = (TextView) findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SendNewChatActivity.this.finish();
			}
		});

		record_button = (RecordButton) findViewById(R.id.record_button);
		record_button
				.setOnFinishedRecordListener(new OnFinishedRecordListener() {

					@Override
					public void onFinishedRecord(String audioPath) {
						dialog.show();
						Log.d("DD onFinishedRecord",
								"onFinishedRecord audioPath ="
										+ currentMediaPath);
						long lastid = DataMgr.getInstance()
								.getLastNewChatServerid(childid);
						new UploadMediaFileIconJob(handler, audioPath,
								JSONConstant.VOICE_TYPE, lastid, childid)
								.execute();
					}

					@Override
					public void onCanceledRecord(String audioPath) {

					}

					@Override
					public void onActionDown() {
						currentMediaPath = Utils
								.getSDCardMediaRootPath(JSONConstant.VOICE_TYPE)
								+ File.separator
								+ Utils.getChatMediaUrl(
										System.currentTimeMillis(),
										JSONConstant.VOICE_TYPE);
						Log.d("DD VOICE", "path =" + currentMediaPath);
						String dir = Utils.getDir(currentMediaPath);
						Utils.makeDirs(dir);
						record_button.setSavePath(currentMediaPath);
					}
				});
	}

	private boolean isEmptyInput() {
		return TextUtils.isEmpty(chatContent.getText().toString());
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
				if (SendNewChatActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.SEND_CHAT_SUCCESS:
					handleSuccess(msg);
					break;
				case EventType.SEND_CHAT_FAIL:
					Toast.makeText(SendNewChatActivity.this,
							R.string.send_fail, Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
			}

		};
	}

	@SuppressWarnings("unchecked")
	private void handleSuccess(Message msg) {
		closeKeyBoard();
		Toast.makeText(SendNewChatActivity.this, R.string.send_success,
				Toast.LENGTH_SHORT).show();
		MyApplication.getInstance().setTmpNewChatList(
				(List<NewChatInfo>) msg.obj);
		setResult(ConstantValue.SEND_CHAT_SUCCESS);
		SendNewChatActivity.this.finish();
	}

	private void runSendChatTask() {
		long lastid = DataMgr.getInstance().getLastNewChatServerid(childid);
		SendChatJob sendChatJob = new SendChatJob(handler,
				InfoHelper.formatChatContent(chatContent.getText().toString(),
						"", childid, ""), childid, lastid);
		sendChatJob.execute();
		dialog.show();
	}

}
