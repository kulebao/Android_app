package com.cocobabys.handler;

import com.cocobabys.R;
import com.cocobabys.activities.ValidatePhoneNumActivity;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.IntentCompat;
import android.widget.Toast;

public class MyHandler extends Handler {

	private Dialog dialog;
	private Activity activity;

	public MyHandler(Activity activity) {
		this(activity, null);
	}

	public MyHandler(Activity activity, Dialog dialog) {
		this.activity = activity;
		this.dialog = dialog;
	}

	@Override
	public void handleMessage(Message msg) {
		// if (activity.isFinishing()) {
		// Log.w("djc", "do nothing when activity finishing!");
		// return;
		// }

		try {
			if (msg.arg2 != ConstantValue.DO_NOT_CANCEL_DIALOG
					&& dialog != null) {
				dialog.cancel();
			}

			switch (msg.what) {
			case EventType.TOKEN_INVALID:
				showRestartDlg(R.string.token_invalid);
				break;
			case EventType.PHONE_NUM_IS_ALREADY_LOGIN:
				//早期不允许一个账号在多个手机上登录，目前允许了
				//所以只有密码发生变化，才会走到这里
				// showRestartDlg(R.string.phone_num_is_already_login);
				showRestartDlg(R.string.pwd_has_changed);
				break;
			case EventType.NET_WORK_INVALID:
				Toast.makeText(activity, R.string.net_error, Toast.LENGTH_SHORT)
						.show();
				break;
			case EventType.SERVER_INNER_ERROR:
				Toast.makeText(activity, R.string.server_error,
						Toast.LENGTH_SHORT).show();
				break;
			case EventType.SERVER_BUSY:
				Toast.makeText(activity, R.string.server_error,
						Toast.LENGTH_SHORT).show();
				break;
			case EventType.AUTH_CODE_IS_INVALID:
				Utils.showSingleBtnEventDlg(EventType.AUTH_CODE_IS_INVALID,
						activity);
				break;
			case EventType.ACCOUNT_IS_EXPIRED:
				showRestartDlg(R.string.phone_invalid);
				break;
			default:
				break;
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}

	private void showRestartDlg(int resID) {
		Utils.showSingleBtnMustConfirmResDlg(resID, activity, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				DataUtils.clearProp();
				DataMgr.getInstance().upgradeAll();
				restartApp();
			}
		});
	}

	private void restartApp() {
		Intent intent = new Intent(activity, ValidatePhoneNumActivity.class);
		ComponentName cn = intent.getComponent();
		Intent mainIntent = IntentCompat.makeRestartActivityTask(cn);
		activity.startActivity(mainIntent);
	}

}
