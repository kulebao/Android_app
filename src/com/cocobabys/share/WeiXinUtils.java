package com.cocobabys.share;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;

public class WeiXinUtils {

	private PlatformActionListener paListener;

	public static WeiXinUtils getInstance() {
		return new WeiXinUtils();
	}

	private WeiXinUtils() {
		paListener = new PlatformActionListener() {
			@Override
			public void onError(Platform arg0, int arg1, Throwable arg2) {

			}

			@Override
			public void onComplete(Platform arg0, int arg1,
					HashMap<String, Object> arg2) {
			}

			@Override
			public void onCancel(Platform arg0, int arg1) {

			}
		};
	}

	public static final String JPG_CONTENT_TYPE = "image/jpeg";

	public void share(String title, String content, String mediumUrl, int type,
			String platform) {
		ShareParams wechat = new ShareParams();
		wechat.setTitle(title);
		wechat.setText(content);
		
		//本地文件的方式，由微博服务器负责上传下载，节约费用。。。
		// wechat.setContentType(Platform.SHARE_IMAGE);
		// wechat.setFilePath(mediumUrl);
		// wechat.setImagePath(mediumUrl);
		
		wechat.setImageUrl(mediumUrl);
		wechat.setShareType(type);

		Platform weixin = ShareSDK.getPlatform(platform);
		weixin.setPlatformActionListener(paListener);
		weixin.share(wechat);
	}
}
