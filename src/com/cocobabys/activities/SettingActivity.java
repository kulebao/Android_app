package com.cocobabys.activities;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cocobabys.R;
import com.cocobabys.adapter.SettingListAdapter;
import com.cocobabys.bean.SettingInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ParentInfo;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.taskmgr.CheckUpdateTask;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.ImageUtils;
import com.cocobabys.utils.Utils;

public class SettingActivity extends UmengStatisticsActivity {
	private Handler handler;
	private ProgressDialog dialog;
	private ArrayList<SettingInfo> firstListinfo;
	private SettingListAdapter adapter;
	private ListView firstlist;
	private ImageView photo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.setting);
		initHead();
		initList();
		initDialog();
		initHandler();

		setTestBtn();
	}

	private void initHead() {
		ParentInfo parentInfo = DataMgr.getInstance().getSelfInfoByPhone();
		TextView relation = (TextView) findViewById(R.id.relation);
		relation.setText(parentInfo.getRelationship());

		TextView name = (TextView) findViewById(R.id.name);
		name.setText(parentInfo.getName());

		TextView phone = (TextView) findViewById(R.id.phone);
		phone.setText(parentInfo.getPhone());

		photo = (ImageView) findViewById(R.id.photo);
		if (!TextUtils.isEmpty(parentInfo.getPortrait())) {
			ImageUtils.getImageLoader().displayImage(parentInfo.getPortrait(), photo);
		}
	}

	private void initList() {
		initData();
		adapter = new SettingListAdapter(this, firstListinfo);
		firstlist = (ListView) findViewById(R.id.firstlist);
		firstlist.setAdapter(adapter);

		firstlist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SettingInfo item = adapter.getItem(position);
				handle(item);
			}
		});
	}

	protected void handle(SettingInfo item) {
		switch (item.getNameid()) {
		case R.string.updateCard:
			updateCard();
			break;
		case R.string.invitation:
			invite();
			break;
		case R.string.check_version:
			runCheckUpdateTask();
			break;
		case R.string.user_response:
			startToFeedBackActivity();
			break;
		case R.string.change_pwd:
			startToChangePWDActivity();
			break;
		case R.string.change_child:
			startToSelectChildActivity();
			break;
		case R.string.about_cocobabys:
			startToAboutUsActivity();
			break;
		case R.string.exit_login:
			Utils.showTwoBtnResDlg(R.string.confirm_exit, this, new android.content.DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					handleExitLogin();
				}

			});
			break;

		default:
			break;
		}
	}

	private void initData() {
		firstListinfo = new ArrayList<SettingInfo>() {
			private static final long serialVersionUID = 1L;

			{
				add(new SettingInfo(R.string.invitation, R.drawable.family_logo));
				add(new SettingInfo(R.string.updateCard, R.drawable.card_logo));
				add(new SettingInfo(R.string.check_version, R.drawable.update_logo));
				add(new SettingInfo(R.string.user_response, R.drawable.feedback_logo));
				add(new SettingInfo(R.string.change_pwd, R.drawable.password_logo));
				add(new SettingInfo(R.string.change_child, R.drawable.change_logo));
				add(new SettingInfo(R.string.about_cocobabys, R.drawable.about_logo));
				add(new SettingInfo(R.string.exit_login, R.drawable.quit_logo));
			}
		};
	}

	private void setTestBtn() {
		if (MyApplication.getInstance().isForTest()) {
			setHostBtn();
			setVideoBtn();
		}
	}

	private void setVideoBtn() {
		final Button changeVideo = (Button) findViewById(R.id.changeVideo);
		changeVideo.setVisibility(View.VISIBLE);
		setVideoBtnText(changeVideo);

		changeVideo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean isMyVideo = Utils.isMyVideo();
				if (isMyVideo) {
					Utils.setVideo("false");
				} else {
					Utils.setVideo("true");
				}
				setVideoBtnText(changeVideo);
			}
		});
	}

	private void setVideoBtnText(final Button changeVideo) {
		if (Utils.isMyVideo()) {
			changeVideo.setText("切换到其他摄像头");
		} else {
			changeVideo.setText("切换到自己的摄像头");
		}
	}

	private void setHostBtn() {
		final Button changeHost = (Button) findViewById(R.id.changeHost);
		changeHost.setVisibility(View.VISIBLE);
		setHostText(changeHost);
		changeHost.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean isTestHost = Utils.isTestHost();
				if (isTestHost) {
					Utils.setToTestHost("false");
				} else {
					Utils.setToTestHost("true");
				}

				DataMgr.getInstance().upgradeAll();
				Utils.clearSDFolder();
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.exit(0);
					}
				}).start();
			}
		});
	}

	private void setHostText(Button hostBtn) {
		if (Utils.isTestHost()) {
			hostBtn.setText("切换到商用地址");
		} else {
			hostBtn.setText("切换到测试地址");
		}
	}

	protected void startToAboutUsActivity() {
		Intent intent = new Intent();
		intent.setClass(this, AboutUsActivity.class);
		startActivity(intent);
	}

	protected void startToFeedBackActivity() {
		Intent intent = new Intent();
		intent.setClass(this, FeedBackActivity.class);
		startActivity(intent);
	}

	private void initDialog() {
		dialog = new ProgressDialog(this);
		dialog.setCancelable(false);
		dialog.setMessage(getResources().getString(R.string.checking_new_version));
	}

	private void initHandler() {

		handler = new MyHandler(this, dialog) {

			@Override
			public void handleMessage(Message msg) {
				if (SettingActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.HAS_NEW_VERSION:
					startToUpdateActivity();
					break;
				case EventType.HAS_NO_VERSION:
					Toast.makeText(SettingActivity.this, R.string.no_new_version, Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
			}
		};
	}

	protected void runCheckUpdateTask() {
		// 如果后台在自动更新，这里可能会有冲突，后续需要把后台任务统一管理起来
		dialog.show();
		DataUtils.saveCheckNewTime(System.currentTimeMillis());
		new CheckUpdateTask(handler, DataUtils.getAccount(), DataUtils.getVersionCode()).execute();
	}

	private void handleExitLogin() {
		DataUtils.clearProp();
		DataMgr.getInstance().upgradeAll();
		Utils.clearSDFolder();
		setResult(ConstantValue.EXIT_LOGIN_RESULT);
		finish();
	}

	private void startToChangePWDActivity() {
		Intent intent = new Intent();
		intent.setClass(this, ChangePWDActivity.class);
		startActivity(intent);
	}

	private void startToUpdateActivity() {
		Intent intent = new Intent();
		intent.setClass(this, UpdateActivity.class);
		startActivity(intent);
	}

	private void startToSelectChildActivity() {
		Intent intent = new Intent();
		intent.setClass(this, ChildListActivity.class);
		startActivity(intent);
	}

	public void updateCard() {
		Intent intent = new Intent();
		intent.setClass(this, CardManagerActivity.class);
		startActivity(intent);
	}

	public void invite() {
		Intent intent = new Intent();
		intent.setClass(this, RelationListActivity.class);
		startActivity(intent);
	}

}
