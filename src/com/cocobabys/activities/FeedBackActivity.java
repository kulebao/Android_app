package com.cocobabys.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cocobabys.R;
import com.cocobabys.constant.EventType;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.taskmgr.FeedBackTask;
import com.cocobabys.utils.Utils;

public class FeedBackActivity extends UmengStatisticsActivity {
	private EditText feedbackContent;
	private MyHandler handler;
	private ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_back);
		ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.feed_back);
		initView();
		initHandler();
	}

	private void initView() {
		initDialog();
		initBtn();
	}

	public void initBtn() {
		feedbackContent = (EditText) findViewById(R.id.feedbackContent);
		Button sendBtn = (Button) findViewById(R.id.sendbtn);
		sendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String content = feedbackContent.getText().toString();
				if (TextUtils.isEmpty(content)) {
					Utils.makeToast(FeedBackActivity.this,
							R.string.invalid_feedback);
					return;
				}
				runFeedBackTask();
			}
		});
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
				if (FeedBackActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.UPLOAD_SUCCESS:
					Toast.makeText(FeedBackActivity.this,
							R.string.send_success, Toast.LENGTH_SHORT).show();
					FeedBackActivity.this.finish();
					break;
				case EventType.UPLOAD_FAILED:
					Toast.makeText(FeedBackActivity.this, R.string.send_fail,
							Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
			}
		};
	}

	private void runFeedBackTask() {
		new FeedBackTask(handler, feedbackContent.getText().toString())
				.execute();
		dialog.show();
	}

}
