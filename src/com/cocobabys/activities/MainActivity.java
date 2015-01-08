package com.cocobabys.activities;

import java.io.File;
import java.util.concurrent.TimeUnit;

import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.cocobabys.R;
import com.cocobabys.bean.AdInfo;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.net.AdMethod;
import com.cocobabys.push.PushModel;
import com.cocobabys.taskmgr.BindPushTask;
import com.cocobabys.taskmgr.CheckUpdateTask;
import com.cocobabys.threadpool.MyThreadPoolMgr;
import com.cocobabys.utils.DataUtils;
import com.cocobabys.utils.Utils;

public class MainActivity extends TabActivity {
	// 5小时检查一次广告更新
	private static final int CHECK_AD_DELAY_TIME = 60 * 60 *5;

	// 每30分钟检查push相关参数是否正常，如不正常，重新绑定
	private static final int CHECK_PUSH_DELAY_TIME = 60 * 30;
	// 启动后，2分钟时，开始检查push相关参数是否正常，如不正常，重新绑定
	private static final int FIRST_DELAY_TIME = 120;
	private TabHost tabHost;
	private static final String TAB_TAG_LOCATION = "location";
	private static final String TAB_TAG_NOTICE = "notice";
	private static final String TAB_TAG_SETTING = "setting";
	private static final String[] TAB_TAGS = { TAB_TAG_NOTICE,
			TAB_TAG_LOCATION, TAB_TAG_SETTING };
	private TabWidget tabWidget;
	private static final int TAB_WIDGET_HEIGHT = 60;
	private int[] labelIds = { R.string.noticeTitle, R.string.locationTitle,
			R.string.setting };

	private Handler handler;
	private AsyncTask<Void, Void, Integer> uodateTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client_tab);
		ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.app_name);
		initUI();
		initHandler();
		checkNew();
		initDirs();
		runCheckBindTask();
		runCheckADTask();
	}

	private void runCheckBindTask() {
		if (!PushModel.getPushModel().isBindInfoSentToServer()) {
			Log.d("DJC", "BindPushTask run !");
			new BindPushTask(handler, DataUtils.getAccount()).execute();
		}

		MyThreadPoolMgr.getGenericService().scheduleWithFixedDelay(
				new Runnable() {
					@Override
					public void run() {
						Utils.bindPush();
					}
				}, FIRST_DELAY_TIME, CHECK_PUSH_DELAY_TIME, TimeUnit.SECONDS);
	}

	private void runCheckADTask() {
		MyThreadPoolMgr.getGenericService().scheduleWithFixedDelay(
				new Runnable() {
					@Override
					public void run() {
						try {
							AdMethod.getMethod().getInfo();
							AdInfo adInfo = DataUtils.getAdInfo();
							if (adInfo != null
									&& !new File(adInfo.getLocalFileName()).exists()) {
								Utils.downloadIcon(adInfo.getImage(),
										adInfo.getLocalFileName());
							}
						} catch (Exception e) {
							Log.e("EEE", "DJC runCheckADTask e="+e.toString());
						}
					}
				}, 0, CHECK_AD_DELAY_TIME, TimeUnit.SECONDS);
	}

	private void initDirs() {
		Utils.makeDefaultDirInSDCard();
	}

	private void checkNew() {
		if (!Utils.isWiFiActive(this)) {
			Log.d("DDD", "wifi closed do nothing!");
			return;
		}
		long checkNewTime = DataUtils.getCheckNewTime();
		long currentTime = System.currentTimeMillis();
		if ((currentTime - checkNewTime) >= ConstantValue.CHECK_NEW_TIME_SPAN) {
			runCheckUpdateTask();
		}
	}

	private void initHandler() {
		handler = new MyHandler(this, null) {
			@Override
			public void handleMessage(Message msg) {
				if (MainActivity.this.isFinishing()) {
					Log.w("djc", "do nothing when activity finishing!");
					return;
				}
				super.handleMessage(msg);
				switch (msg.what) {
				case EventType.HAS_NEW_VERSION:
					Utils.showTwoBtnResDlg(R.string.update_now,
							MainActivity.this, new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									startToUpdateActivity();
								}
							});
					break;
				case EventType.SERVER_INNER_ERROR:
					Toast.makeText(MainActivity.this,
							R.string.get_child_info_fail, Toast.LENGTH_SHORT)
							.show();
					break;

				default:
					break;
				}
			}

		};
	}

	private void startToUpdateActivity() {
		Intent intent = new Intent();
		intent.setClass(this, UpdateActivity.class);
		startActivity(intent);
	}

	private void runCheckUpdateTask() {
		DataUtils.saveCheckNewTime(System.currentTimeMillis());
		uodateTask = new CheckUpdateTask(handler, DataUtils.getAccount(),
				DataUtils.getVersionCode()).execute();
	}

	private void initUI() {
		tabHost = getTabHost();

		int[] iconIds = { R.drawable.ic_launcher, R.drawable.ic_launcher,
				R.drawable.ic_launcher };
		Class<?>[] classes = { SchoolNoticeActivity.class,
				CookBookActivity.class, SettingActivity.class };
		Resources res = this.getResources();
		for (int i = 0; i < TAB_TAGS.length; ++i) {
			View view = LayoutInflater.from(this).inflate(R.layout.tab_widget,
					null);
			TextView titleView = (TextView) view.findViewById(R.id.title);
			ImageView iconView = (ImageView) view.findViewById(R.id.icon);
			if (i == 0) {
				titleView.setTextColor(Color.BLACK);
			} else {
				titleView.setTextColor(Color.WHITE);
			}
			titleView.setText(res.getString(labelIds[i]));
			iconView.setBackgroundDrawable(res.getDrawable(iconIds[i]));
			appendIntentToTab(classes[i], TAB_TAGS[i], view);
		}

		setTabWidgetParams();
		setTabChangedListener();
	}

	private void appendIntentToTab(Class<?> toActivity, String tabTag, View view) {
		tabHost.addTab(tabHost.newTabSpec(tabTag).setIndicator(view)
				.setContent(new Intent(MainActivity.this, toActivity)));
	}

	private void setTabWidgetParams() {
		tabWidget = getTabWidget();
		for (int i = 0; i < tabWidget.getChildCount(); i++) {
			// 设置高度、宽度，不过宽度由于设置为fill_parent，在此对它没效果
			tabWidget.getChildAt(i).getLayoutParams().height = TAB_WIDGET_HEIGHT;
		}
	}

	private void setTabChangedListener() {
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				int id = tabHost.getCurrentTab();
				for (int i = 0; i < TAB_TAGS.length; ++i) {
					View view = tabHost.getTabWidget().getChildAt(i);
					TextView textview = (TextView) view
							.findViewById(R.id.title);
					if (i != id) {
						textview.setTextColor(Color.WHITE);
					} else {
						// setTabTitle(id);
						textview.setTextColor(Color.BLACK);
					}
				}
			}
		});
	}

	public AsyncTask<Void, Void, Integer> getUpdateTask() {
		return uodateTask;
	}

}
