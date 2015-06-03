package com.cocobabys.share;

import java.util.HashMap;

import android.graphics.BitmapFactory;
import android.util.Log;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;

import com.cocobabys.R;
import com.cocobabys.activities.MyApplication;

public class WeiXinUtils{

    private PlatformActionListener paListener;

    public static WeiXinUtils getInstance(){
        return new WeiXinUtils();
    }

    private WeiXinUtils(){
        paListener = new PlatformActionListener(){
            @Override
            public void onError(Platform arg0, int arg1, Throwable arg2){
                // Utils.makeToast(MyApplication.getInstance(), "err code=" +
                // arg1);
                Log.e("", "djcweixin arg2=" + arg2.getMessage() + " arg1=" + arg1);
            }

            @Override
            public void onComplete(Platform arg0, int arg1, HashMap<String, Object> arg2){
                // Utils.makeToast(MyApplication.getInstance(), "onComplete");
                Log.e("", "djcweixin onComplete");
            }

            @Override
            public void onCancel(Platform arg0, int arg1){
                // Utils.makeToast(MyApplication.getInstance(), "onCancel arg1="
                // + arg1);
                Log.e("", "djcweixin onCancel arg1 =" + arg1);
            }
        };
    }

    public static final String JPG_CONTENT_TYPE = "image/jpeg";

    // 分享本地文件
    public void shareFile(String title, String content, String mediumUrl, int type, String platform){
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
    public void shareUrl(String title, String content, String mediumUrl, int type, String platform){
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
    public void shareWebPage(String title, String content, String mediumUrl, String platform){
        ShareParams wechat = new ShareParams();
        wechat.setTitle(title);
        wechat.setText(content);

        mediumUrl = mediumUrl.replace("https", "http");

        wechat.setUrl(mediumUrl);
        wechat.setImageData(BitmapFactory.decodeResource(MyApplication.getInstance().getResources(), R.drawable.logo));
        wechat.setShareType(Platform.SHARE_WEBPAGE);

        Platform weixin = ShareSDK.getPlatform(platform);
        Log.w("", "djcweixin mediumUrl =" + mediumUrl + " type=" + Platform.SHARE_WEBPAGE + " platform=" + platform);
        weixin.setPlatformActionListener(paListener);
        weixin.share(wechat);
    }

}