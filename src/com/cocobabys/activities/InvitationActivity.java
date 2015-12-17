package com.cocobabys.activities;

import com.alibaba.fastjson.JSONObject;
import com.cocobabys.R;
import com.cocobabys.bean.FamilyInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.customview.CountDownButton;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.jobs.InvitationJob;
import com.cocobabys.jobs.PostInviteCodeJob;
import com.cocobabys.utils.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class InvitationActivity extends UmengStatisticsActivity {

	private MyHandler handler;
	private ProgressDialog dialog;
	private CountDownButton countDownButton;
	private EditText inputphone;
	private EditText inuputAuthCode;
	private EditText inuputRelation;
	private EditText relation_name;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invitation);
		ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.invitation);
		initDlg();
		initHandler();
		initUI();
	}

	private void initDlg() {
		dialog = new ProgressDialog(this);
		dialog.setMessage(getResources().getString(R.string.inviting));
	}

	private void initHandler() {
		handler = new MyHandler(this, dialog) {
			@Override
			public void handleMessage(Message msg) {
				if (InvitationActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.INVITE_FAIL:
					Utils.showSingleBtnResDlg(R.string.inviteFail, InvitationActivity.this);
					break;
				case EventType.INVITE_SUCCESS:
					handleInviteSuccess();
					break;
				case EventType.INVITE_PHONE_ALREADY_EXIST:
					Utils.showSingleBtnResDlg(R.string.phoneAlreadyReg, InvitationActivity.this);
					break;
				case EventType.GET_INVITED_CODE_FAIL_PHONE_ALREADY_EXIST:
					Utils.showSingleBtnResDlg(R.string.postInvitedCodeFail, InvitationActivity.this);
					break;

				case EventType.INVITE_PHONE_INVALID:
					Utils.showSingleBtnResDlg(R.string.invite_phone_invalid, InvitationActivity.this);
					countDownButton.enableGetAuthBtn();
					break;
				case EventType.GET_AUTH_CODE_SUCCESS:
					handleGetAuthCodeSuccess();
					break;
				case EventType.GET_AUTH_CODE_FAIL:
					handleGetAuthCodeFail(msg.what);
					break;
				case EventType.GET_AUTH_CODE_TOO_OFTEN:
					handleGetAuthCodeFail(msg.what);
					break;

				default:
					break;
				}
			}

		};
	}

	private void handleInviteSuccess() {
		// Utils.showSingleBtnResDlg(R.string.inviteSuccess,
		// InvitationActivity.this);

		Utils.makeToast(this, R.string.inviteSuccess);
		FamilyInfo relaiton = new FamilyInfo();

		relaiton.setName(relation_name.getText().toString());
		relaiton.setPhone(inputphone.getText().toString());
		relaiton.setRelation(inuputRelation.getText().toString());

		Intent data = new Intent();
		data.putExtra(ConstantValue.RELATION_INFO, JSONObject.toJSONString(relaiton));
		setResult(RESULT_OK, data);

		finish();
		// inuputAuthCode.setText("");
		// inputphone.setText("");
		// inuputRelation.setText("");
		// relation_name.setText("");
	}

	private void handleGetAuthCodeSuccess() {
		Toast.makeText(this, R.string.postInvitedCodeSuccess, Toast.LENGTH_SHORT).show();
		countDownButton.countdown();
	}

	private void handleGetAuthCodeFail(int eventtype) {
		Utils.showSingleBtnEventDlg(eventtype, this);
		countDownButton.enableGetAuthBtn();
	}

	private void initUI() {
		countDownButton = (CountDownButton) findViewById(R.id.sendAuthCode);
		inputphone = (EditText) findViewById(R.id.inuputphoneView);
		inuputAuthCode = (EditText) findViewById(R.id.inuputAuthCode);
		inuputRelation = (EditText) findViewById(R.id.inuputRelation);
		relation_name = (EditText) findViewById(R.id.relation_name);
	}

	public void confirmInvite(View view) {
		String phone = inputphone.getText().toString();
		if (!Utils.checkPhoneNum(phone)) {
			Utils.showSingleBtnEventDlg(EventType.PHONE_NUM_INPUT_ERROR, this);
			return;
		}

		String authcode = inuputAuthCode.getText().toString();
		if (!Utils.checkAuthCode(authcode)) {
			Utils.showSingleBtnEventDlg(EventType.AUTH_CODE_INPUT_ERROR, this);
			return;
		}

		String relation = inuputRelation.getText().toString().trim();
		if (TextUtils.isEmpty(relation)) {
			Utils.showSingleBtnResDlg(R.string.invalidRelation, this);
			return;
		}

		String name = relation_name.getText().toString().trim();
		if (TextUtils.isEmpty(name)) {
			Utils.showSingleBtnResDlg(R.string.invalidName, this);
			return;
		}

		dialog.show();
		InvitationJob invitationJob = new InvitationJob(handler, phone, name, relation, authcode);
		invitationJob.execute();
	}

	// 向被邀请人手机发送验证码
	public void sendAuthCode(View view) {
		runGetAuthCodeTask();
	}

	public void getPhone(View view) {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Cursor cursor = null;
		if (resultCode == Activity.RESULT_OK) {
			if (data == null) {
				Log.d("", "DD onActivityResult data is null ");
				return;
			}
			handlePhoneData(data, cursor);
		}
	}

	private void handlePhoneData(Intent data, Cursor cursor) {
		try {
			Uri uri = data.getData();
			cursor = getContentResolver().query(uri, null, null, null, null);
			cursor.moveToFirst();
			String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

			Log.d("", "name =" + name);

			String fixedNum = getFixed(number);
			Log.d("", "fixedNum =" + fixedNum);

			if (Utils.checkPhoneNum(fixedNum)) {
				inputphone.setText(fixedNum);
			} else {
				String content = String.format(Utils.getResString(R.string.get_invalid_phone), number);
				Utils.showSingleBtnResDlg(content, this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	private String getFixed(String number) {
		if (TextUtils.isEmpty(number)) {
			return number;
		}

		number = number.trim().replace("-", "");

		if (number.startsWith("86") || number.startsWith("+86")) {
			number = number.replace("+86", "");
			number = number.replace("86", "");
		}

		number = number.trim();
		return number;
	}

	private void runGetAuthCodeTask() {
		String phoneNum = inputphone.getText().toString();
		if (!Utils.checkPhoneNum(phoneNum)) {
			Utils.showSingleBtnEventDlg(EventType.PHONE_NUM_INPUT_ERROR, this);
			return;
		}

		dialog.setMessage(getResources().getString(R.string.post_invitation_code));
		dialog.show();
		new PostInviteCodeJob(handler, phoneNum).execute();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		countDownButton.cancel();
	}

}
