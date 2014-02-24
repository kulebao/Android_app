package com.djc.logintest.activities;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.djc.logintest.R;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.ChatInfo;
import com.djc.logintest.handler.MyHandler;
import com.djc.logintest.taskmgr.SendChatTask;
import com.djc.logintest.utils.Utils;

public class SendChatActivity extends Activity {
	private EditText chatContent;
	private MyHandler handler;
	private ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.write_notice);
		initView();
		initHandler();
	}

	private void initView() {
		initDialog();
		initBtn();
	}

	public void initBtn() {
		chatContent = (EditText) findViewById(R.id.edit_notice);
		TextView sendBtn = (TextView) findViewById(R.id.send);
		sendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isEmptyInput()) {
					Toast.makeText(SendChatActivity.this,
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
				SendChatActivity.this.finish();
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
				if (SendChatActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.SUCCESS:
					handleSuccess(msg);
					break;
				case EventType.FAIL:
					Toast.makeText(SendChatActivity.this, R.string.send_fail,
							Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
			}

		};
	}

	private void handleSuccess(Message msg) {
		Toast.makeText(SendChatActivity.this, R.string.send_success,
				Toast.LENGTH_SHORT).show();
		MyApplication.getInstance().setTmpList((List<ChatInfo>) msg.obj);
		setResult(ConstantValue.SEND_CHAT_SUCCESS);
		SendChatActivity.this.finish();
	}

	private void runSendChatTask() {
		int lastid = DataMgr.getInstance().getLastChatServerid();
		new SendChatTask(handler, formatChatContent(), lastid).execute();
		dialog.show();
	}

	private String formatChatContent() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(ChatInfo.CONTENT, chatContent.getText().toString());
			jsonObject.put("phone", Utils.getProp(JSONConstant.ACCOUNT_NAME));
			jsonObject.put(JSONConstant.TIME_STAMP, System.currentTimeMillis());
			jsonObject.put(ChatInfo.SENDER, "");
			jsonObject.put("image", "");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject.toString();
	}

}
