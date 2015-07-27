package com.cocobabys.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.cocobabys.R;
import com.cocobabys.bean.ActionInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.DoEnrollJob;
import com.cocobabys.jobs.GetEnrollJob;
import com.cocobabys.utils.ImageUtils;
import com.cocobabys.utils.Utils;

public class ActionActivity extends UmengStatisticsActivity {
	private Handler handler;
	private TextView titleView;
	private ProgressDialog dialog;
	private TextView contactView;
	private TextView detailView;
	private Button enrollBtn;
	private ActionInfo actioninfo;
	private ImageView actionImageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.action_detail);
		Log.d("DDD JJJ", "NoticeActivity onCreate");
		initData();

		initView();

		initDlg();

		initHandler();

		runCheckEnrollTask();
	}

	private void runCheckEnrollTask() {
		GetEnrollJob enrollJob = new GetEnrollJob(handler, actioninfo.getId());
		enrollJob.execute();
	}

	private void initData() {
		String detail = getIntent().getStringExtra(ConstantValue.ACTION_DETAIL);
		actioninfo = JSON.parseObject(detail, ActionInfo.class);
	}

	private void initHandler() {
		handler = new MyHandler(this, dialog) {
			@Override
			public void handleMessage(Message msg) {
				if (ActionActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.ACTION_ENROLLED:
					handleEnrolled();
					break;
				case EventType.ACTION_NOT_ENROLL:
					break;
				case EventType.ACTION_GET_ENROLL_FAIL:
					break;
				case EventType.ACTION_DO_ENROLL_FAIL:
					Utils.makeToast(ActionActivity.this, R.string.enroll_fail);
					break;
				case EventType.ACTION_DO_ENROLL_SUCCESS:
					handleEnrolled();
					Utils.makeToast(ActionActivity.this,
							R.string.enroll_success);
					break;
				default:
					break;
				}
			}

		};
	}

	private void initDlg() {
		dialog = new ProgressDialog(this);
		dialog.setMessage(Utils.getResString(R.string.enrolling));
		dialog.setCancelable(true);
	}

	private void initView() {
		setLogo();

		initContent();

		initBtn();
	}

	private void initContent() {
		titleView = (TextView) findViewById(R.id.title);
		titleView.setText(actioninfo.getTitle());

		contactView = (TextView) findViewById(R.id.contact);
		contactView.setText(actioninfo.getContact());

		detailView = (TextView) findViewById(R.id.detail);
		detailView.setText(actioninfo.getDetail());
	}

	private void initBtn() {
		enrollBtn = (Button) findViewById(R.id.enroll);

		enrollBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				runEnrollTask();
			}
		});
	}

	private void runEnrollTask() {
		dialog.show();
		DoEnrollJob doEnrollJob = new DoEnrollJob(handler, actioninfo);
		doEnrollJob.execute();
	}

	private void setLogo() {
		actionImageView = (ImageView) findViewById(R.id.actionImage);
		if (!TextUtils.isEmpty(actioninfo.getLogo())) {
			ImageUtils.getImageLoader().displayImage(actioninfo.getLogo(),
					actionImageView);
		}
	}

	private void handleEnrolled() {
		enrollBtn.setText(R.string.enrolled);
		enrollBtn.setEnabled(false);
		enrollBtn.setBackgroundResource(R.drawable.already_feedback);
	}

}
