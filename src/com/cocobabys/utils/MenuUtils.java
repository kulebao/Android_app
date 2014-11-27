package com.cocobabys.utils;

import android.app.Activity;
import android.graphics.Canvas;

import com.cocobabys.R;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;

public class MenuUtils {
	// create animate slidemenu
	public static void createAnimateSlideMenu(CanvasTransformer mTransformer, SlidingMenu menu, Activity activity) {
		mTransformer = new CanvasTransformer() {
			@Override
			public void transformCanvas(Canvas canvas, float percentOpen) {
				canvas.scale(percentOpen, 1, 0, 0);
			}
		};
		menu = new SlidingMenu(activity);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		menu.setBehindOffsetRes(R.dimen.slidingmenu_no_offset);
		menu.setBehindScrollScale(0.0f);
		menu.setBehindCanvasTransformer(mTransformer);
		menu.attachToActivity(activity, SlidingMenu.SLIDING_CONTENT);
		menu.setMenu(R.layout.menu_frame);
	}
}
