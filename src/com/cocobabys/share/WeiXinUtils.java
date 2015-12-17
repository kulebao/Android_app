package com.cocobabys.share;

import java.util.HashMap;

import com.cocobabys.R;
import com.cocobabys.activities.MyApplication;
import com.cocobabys.bean.ShareToken;
import com.cocobabys.utils.Utils;

import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.wechat.moments.WechatMoments;

public class WeiXinUtils {

	private PlatformActionListener paListener;

	public static WeiXinUtils getInstance() {
		return new WeiXinUtils();
	}

	private WeiXinUtils() {
		paListener = new PlatformActionListener() {
			@Override
			public void onError(Platform arg0, int arg1, Throwable arg2) {
				// Utils.makeToast(MyApplication.getInstance(), "err code=" +
				// arg1);
				Log.e("", "djcweixin arg2=" + arg2.getMessage() + " arg1=" + arg1);
			}

			@Override
			public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2) {
				// Utils.makeToast(MyApplication.getInstance(), "onComplete");
				Log.e("", "djcweixin onComplete");
			}

			@Override
			public void onCancel(Platform arg0, int arg1) {
				// Utils.makeToast(MyApplication.getInstance(), "onCancel arg1="
				// + arg1);
				Log.e("", "djcweixin onCancel arg1 =" + arg1);
			}
		};
	}

	public static final String JPG_CONTENT_TYPE = "image/jpeg";

	// 分享本地文件
	public void shareFile(String title, String content, String mediumUrl, int type, String platform) {
		ShareParams wechat = new ShareParams();
		wechat.setTitle(title);
		wechat.setText(content);

		// 本地文件的方式，由微信服务器负责上传下载，节约费用。。。
		wechat.setFilePath(mediumUrl);
		wechat.setImagePath(mediumUrl);
		// wechat.setImageUrl(mediumUrl);

		wechat.setShareType(type);

		Platform weixin = ShareSDK.getPlatform(platform);
		Log.w("", "djcweixin mediumUrl =" + mediumUrl + " type=" + type + " platform=" + platform);
		weixin.setPlatformActionListener(paListener);
		weixin.share(wechat);
	}

	// 分享链接
	public void shareUrl(String title, String content, String mediumUrl, int type, String platform) {
		ShareParams wechat = new ShareParams();
		wechat.setTitle(title);
		wechat.setText(content);

		mediumUrl = mediumUrl.replace("https", "http");

		// 本地文件的方式，由微信服务器负责上传下载，节约费用。。。
		wechat.setImageUrl(mediumUrl);
		wechat.setUrl(mediumUrl);
		wechat.setImageData(BitmapFactory.decodeResource(MyApplication.getInstance().getResources(), R.drawable.logo));
		wechat.setShareType(type);

		Platform weixin = ShareSDK.getPlatform(platform);
		Log.w("", "djcweixin mediumUrl =" + mediumUrl + " type=" + type + " platform=" + platform);
		weixin.setPlatformActionListener(paListener);
		weixin.share(wechat);
	}

	// 分享网页
	public void shareWebPage(String title, String content, String mediumUrl, String platform) {
		ShareParams wechat = new ShareParams();

		if (TextUtils.isEmpty(title)) {
			title = Utils.getResString(R.string.share_title);
		}
		wechat.setTitle(title);

		wechat.setText(content);

		mediumUrl = mediumUrl.replace("https", "http");

		wechat.setUrl(mediumUrl);

		wechat.setImageData(BitmapFactory.decodeResource(MyApplication.getInstance().getResources(),
				R.drawable.logo_weixin));

		wechat.setShareType(Platform.SHARE_WEBPAGE);

		Platform weixin = ShareSDK.getPlatform(platform);
		Log.w("", "djcweixin mediumUrl =" + mediumUrl + " type=" + Platform.SHARE_WEBPAGE + " platform=" + platform);
		weixin.setPlatformActionListener(paListener);
		weixin.share(wechat);
	}

	// 分享网页
	public void shareWebPage(ShareToken shareToken, String platform) {
		ShareParams wechat = new ShareParams();

		String content = shareToken.getContent();
		String mediumUrl = shareToken.buildShareUrl();

		String title = getTitle(shareToken, platform);

		wechat.setTitle(title);

		wechat.setText(content);

		wechat.setUrl(mediumUrl);

		wechat.setImageData(shareToken.getBitmap());

		wechat.setShareType(Platform.SHARE_WEBPAGE);

		Platform weixin = ShareSDK.getPlatform(platform);

		Log.w("", "djcweixin mediumUrl =" + mediumUrl + " type=" + Platform.SHARE_WEBPAGE + " platform=" + platform);

		weixin.setPlatformActionListener(paListener);
		weixin.share(wechat);
	}

	private String getTitle(ShareToken shareToken, String platform) {
		String title = shareToken.getTitle();
		if (TextUtils.isEmpty(title)) {
			title = Utils.getResString(R.string.share_title);
		}

		// 如果是分享到朋友圈，content不起作用，只有title。如果这时content不为空
		// 则把content当做title
		if (WechatMoments.NAME.equals(platform) && !TextUtils.isEmpty(shareToken.getContent())) {
			title = shareToken.getContent();
		}

		return title;
	}

}
