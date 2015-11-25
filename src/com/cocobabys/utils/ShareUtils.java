package com.cocobabys.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

public class ShareUtils {
	/**
	 * 分享功能
	 * 
	 * @param context
	 *            上下文
	 * @param activityTitle
	 *            Activity的名字
	 * @param msgTitle
	 *            消息标题
	 * @param msgText
	 *            消息内容
	 * @param imgPath
	 *            图片路径，不分享图片则传null
	 */
	public static void shareMsg(Context context, String activityTitle, String msgTitle, String msgText, String imgPath) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		if (imgPath == null || imgPath.equals("")) {
			intent.setType("text/plain"); // 纯文本
		} else {
			File f = new File(imgPath);
			if (f != null && f.exists() && f.isFile()) {
				intent.setType("image/png");
				Uri u = Uri.fromFile(f);
				intent.putExtra(Intent.EXTRA_STREAM, u);
			}
		}
		intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
		intent.putExtra(Intent.EXTRA_TEXT, msgText);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(Intent.createChooser(intent, activityTitle));
	}

	public static void shareMsgEx(Context context, String activityTitle, String msgTitle, String msgText, String imgPath) {
		Intent tmp = initIntent(imgPath);

		List<String> packageName = new ArrayList<String>();

		// gets the list of intents that can be loaded.
		List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(tmp, 0);
		if (!resInfo.isEmpty()) {
			List<Intent> targetedShareIntents = new ArrayList<Intent>();
			for (ResolveInfo info : resInfo) {
				Intent intent = initIntent(imgPath);
				Log.d("", "package name =" + info.activityInfo.name);
				Log.d("", "package name =" + info.activityInfo.packageName);

				String lowerCase = info.activityInfo.packageName.toLowerCase();

				if (!packageName.contains(lowerCase) && validPackageName.contains(lowerCase)) {
					intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
					intent.putExtra(Intent.EXTRA_TEXT, msgText);
					intent.setPackage(info.activityInfo.packageName);
					packageName.add(lowerCase);
					Log.d("", "targetedShareIntents add name =" + info.activityInfo.packageName);
					targetedShareIntents.add(intent);
				}
			}

			if (targetedShareIntents.isEmpty()) {
				Utils.makeToast(context, "没有找到可以分享的应用，请先安装QQ或者微信，谢谢！");
				return;
			}

			Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), activityTitle);
			if (chooserIntent == null) {
				Utils.makeToast(context, "没有找到可以分享的应用，请先安装QQ或者微信，谢谢！");
				return;
			}
			chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[] {}));

			try {
				context.startActivity(chooserIntent);
			} catch (android.content.ActivityNotFoundException ex) {
				Utils.makeToast(context, "没有找到可以分享的应用，请先安装QQ或者微信，谢谢！");
			}
		} else {
			Utils.makeToast(context, "没有找到可以分享的应用，请先安装QQ或者微信，谢谢！");
			return;
		}
	}

	private static Intent initIntent(String imgPath) {
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);

		if (imgPath == null || imgPath.equals("")) {
			intent.setType("text/plain"); // 纯文本
		} else {
			File f = new File(imgPath);
			if (f != null && f.exists() && f.isFile()) {
				intent.setType("image/png");
				Uri u = Uri.fromFile(f);
				intent.putExtra(Intent.EXTRA_STREAM, u);
			}
		}
		return intent;
	}

	// {
	// String contentDetails = "";
	// String contentBrief = "";
	// String shareUrl = "";
	// Intent it = new Intent(Intent.ACTION_SEND);
	// it.setType("text/plain");
	// List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(it,
	// 0);
	// if (!resInfo.isEmpty()) {
	// List<Intent> targetedShareIntents = new ArrayList<Intent>();
	// for (ResolveInfo info : resInfo) {
	// Intent targeted = new Intent(Intent.ACTION_SEND);
	// targeted.setType("text/plain");
	// ActivityInfo activityInfo = info.activityInfo;
	//
	// // judgments : activityInfo.packageName, activityInfo.name, etc.
	// if (activityInfo.packageName.contains("bluetooth") ||
	// activityInfo.name.contains("bluetooth")) {
	// continue;
	// }
	// if (activityInfo.packageName.contains("gm") ||
	// activityInfo.name.contains("mail")) {
	// targeted.putExtra(Intent.EXTRA_TEXT, contentDetails);
	// } else if (activityInfo.packageName.contains("zxing")) {
	// targeted.putExtra(Intent.EXTRA_TEXT, shareUrl);
	// } else {
	// targeted.putExtra(Intent.EXTRA_TEXT, contentBrief);
	// }
	// targeted.setPackage(activityInfo.packageName);
	// targetedShareIntents.add(targeted);
	// }
	// Intent chooserIntent =
	// Intent.createChooser(targetedShareIntents.remove(0),
	// "Select app to share");
	// if (chooserIntent == null) {
	// return;
	// }
	// // A Parcelable[] of Intent or LabeledIntent objects as set with
	// // putExtra(String, Parcelable[]) of additional activities to place
	// // a the front of the list of choices, when shown to the user with a
	// // ACTION_CHOOSER.
	// chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
	// targetedShareIntents.toArray(new Parcelable[] {}));
	// try {
	// startActivity(chooserIntent);
	// } catch (android.content.ActivityNotFoundException ex) {
	// Toast.makeText(this, "Can't find share component to share",
	// Toast.LENGTH_SHORT).show();
	// }
	// }
	// }

	private static List<String> validPackageName = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;

		{
			add("com.android.mms");
			add("com.tencent.mm");
			add("com.tencent.mobileqq");
			add("com.sina.weibo");
		}
	};
}
