package com.cocobabys.activities;

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

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ActionActivity extends NavigationActivity {
	private Handler handler;
	private TextView titleView;
	private ProgressDialog dialog;
	private TextView contactView;
	private TextView detailView;
	private Button enrollBtn;
	private ActionInfo actioninfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.action_detail);
		ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.action_detail);
		initData();

		initView();

		initDlg();

		initHandler();

		runCheckEnrollTask();

		setEndPoint(actioninfo);

		// 服务器存反了，这里临时处理一下，等服务器改了再调整
		// setEndPoint(new LatLng(actioninfo.getLocation().getLongitude(),
		// actioninfo.getLocation().getLatitude()));
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
					Utils.makeToast(ActionActivity.this, R.string.enroll_success);
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
		setTitle();

		setPrice();

		setContact();

		setAddress();

		setDetail();
	}

	private void setAddress() {
		TextView address = (TextView) findViewById(R.id.address);
		address.setText(actioninfo.getAddress());
	}

	private void setDetail() {
		detailView = (TextView) findViewById(R.id.detail);
		detailView.setText(actioninfo.getDetail());
	}

	private void setContact() {
		contactView = (TextView) findViewById(R.id.contact);
		contactView.setText(actioninfo.getContact());
	}

	public void contact(View view) {
		Utils.startToCall(this, actioninfo.getContact());
	}

	public void navigation(View view) {
		startRoutePlanDriving();
	}

	private void setPrice() {
		setPretext();

		TextView discountprice = setOriginalPrice();

		discountprice.setText("¥" + Utils.doubleToString(actioninfo.getPrice().getDiscounted()));
	}

	private void setPretext() {
		TextView pretext = (TextView) findViewById(R.id.pretext);

		SpannableString spanString = new SpannableString(Utils.getResString(R.string.special_discount_1));
		// AbsoluteSizeSpan span = new AbsoluteSizeSpan(36);
		RelativeSizeSpan span = new RelativeSizeSpan(1.5f);

		spanString.setSpan(span, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		// ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.BLUE);
		ForegroundColorSpan colorSpan = new ForegroundColorSpan(0xff22ace1);

		spanString.setSpan(colorSpan, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		StyleSpan styleSpan = new StyleSpan(android.graphics.Typeface.BOLD);
		spanString.setSpan(styleSpan, 0, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		pretext.append(spanString);
	}

	private TextView setOriginalPrice() {
		TextView originalprice = (TextView) findViewById(R.id.originalprice);

		originalprice.setText(Utils.doubleToString(actioninfo.getPrice().getOrigin()) + "");

		originalprice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); // 中划线

		originalprice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG); // 设置中划线并加清晰

		TextView discountprice = (TextView) findViewById(R.id.discountprice);
		return discountprice;
	}

	private void setTitle() {
		titleView = (TextView) findViewById(R.id.title);
		titleView.setText(actioninfo.getTitle());
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
		ImageView actionImageView = (ImageView) findViewById(R.id.actionImage);
		TextView count = (TextView) findViewById(R.id.count);

		if (!actioninfo.getLogos().isEmpty()) {
			// imageLoader.displayImage(item.getLogo(), flagholder.imageView);
			ImageUtils.displayEx(actioninfo.getLogos().get(0).getUrl(), actionImageView,
					ConstantValue.ACTION_PIC_MAX_WIDTH, ConstantValue.ACTION_PIC_MAX_HEIGHT);
			count.setText(actioninfo.getLogos().size() + "张");
		} else {
			count.setVisibility(View.GONE);
		}

		actionImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!actioninfo.getLogos().isEmpty()) {
					Intent intent = new Intent(ActionActivity.this, MyGalleryActivity.class);
					String businessinfo = JSON.toJSONString(actioninfo);
					intent.putExtra(ConstantValue.BUSINESS_INFO, businessinfo);
					ActionActivity.this.startActivity(intent);
				}
			}
		});
	}

	private void handleEnrolled() {
		enrollBtn.setText(R.string.enrolled);
		enrollBtn.setEnabled(false);
		enrollBtn.setBackgroundResource(R.drawable.already_feedback);
	}

}
