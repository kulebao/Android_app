package com.cocobabys.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cocobabys.R;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.BindCardJob;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

public class CardManagerActivity extends UmengStatisticsActivity {
	private Handler handler;
	private ProgressDialog dialog;
	private EditText inputCardNum;
	private Button bindCard;
	private String card = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.card_manager);
		ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.updateCard);
		initView();
		initDialog();
		initHandler();
	}

	private void initView() {
		inputCardNum = (EditText) findViewById(R.id.inputCardNum);
		bindCard = (Button) findViewById(R.id.bindCard);

		String card = DataUtils.getCard();

		Log.d("", "initView card=" + card);

		if (Utils.checkCardNum(card)) {
			inputCardNum.setText(card);
			// bindCard.setText(Utils.getResString(R.string.cardAlreadyBind));
			// bindCard.setEnabled(false);
			// inputCardNum.setEnabled(false);
		}
	}

	private void initDialog() {
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage(getResources().getString(R.string.bindingCard));
	}

	private void initHandler() {

		handler = new MyHandler(this, dialog) {

			@Override
			public void handleMessage(Message msg) {
				if (CardManagerActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.BIND_CARD_FAIL:
					Utils.showSingleBtnResDlg(R.string.bindCardFail, CardManagerActivity.this);
					break;
				case EventType.BIND_DUPLICATED:
					Utils.showSingleBtnResDlg(R.string.bindDup, CardManagerActivity.this);
					break;
				case EventType.BIND_CARD_INVALID:
					Utils.showSingleBtnResDlg(R.string.cardInvalid, CardManagerActivity.this);
					break;
				case EventType.BIND_CARD_SUCCESS:
					Toast.makeText(CardManagerActivity.this, R.string.bindCardSuccess, Toast.LENGTH_SHORT).show();
					DataMgr instance = DataMgr.getInstance();
					instance.updateCardNum(card, instance.getSelfInfoByPhone().getParent_id());
					CardManagerActivity.this.finish();
					break;
				default:
					break;
				}
			}
		};
	}

	public void bindCard(View view) {
		card = inputCardNum.getText().toString();
		if (!Utils.checkCardNum(card)) {
			Utils.showSingleBtnResDlg(R.string.invalidCardNum, this);
			return;
		}

		dialog.show();

		BindCardJob bindCardJob = new BindCardJob(handler, card);
		bindCardJob.execute();
	}

}
