package com.cocobabys.activities;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

import com.cocobabys.R;

public class NoticeTabActivity extends TabActivity {

	private TabHost tabHost;
	private static final String TAB_TAG_NOTICE = "notice";
	private static final String TAB_TAG_HOMEWORK = "homework";
	private static final String[] TAB_TAGS = { TAB_TAG_NOTICE, TAB_TAG_HOMEWORK };
	private TabWidget tabWidget;
	private static final int TAB_WIDGET_HEIGHT = 80;
	private int[] labelIds = { R.string.pnotice, R.string.homework };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notice_tab);
		initUI();
	}

	private void startToUpdateActivity() {
		Intent intent = new Intent();
		intent.setClass(this, UpdateActivity.class);
		startActivity(intent);
	}

	private void initUI() {
		tabHost = getTabHost();

		int[] iconIds = { R.drawable.ic_launcher, R.drawable.ic_launcher };
		Class<?>[] classes = { NoticePullRefreshActivity.class,
				HomeworkPullRefreshActivity.class };
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
		Intent intent = new Intent(NoticeTabActivity.this, toActivity);
		tabHost.addTab(tabHost.newTabSpec(tabTag).setIndicator(view)
				.setContent(intent));
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
				Log.d("", "onTabChanged tabId=" + tabId);

				// 切换到公告时，重新设置一下intent，以免NoticePullRefreshActivity在tab内切换时，每次
				// 都执行onNewIntent去加载数据
				if (TAB_TAG_NOTICE.equals(tabId)) {
					Intent intent = new Intent();
					NoticeTabActivity.this.setIntent(intent);
				}
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

}
