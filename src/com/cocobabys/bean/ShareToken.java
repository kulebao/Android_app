package com.cocobabys.bean;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cocobabys.R;
import com.cocobabys.activities.MyApplication;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.ExpInfo;
import com.cocobabys.utils.Utils;

public class ShareToken{
    // 对应成长经历的服务器id
    private long   id      = 0;

    private String token   = "";
    private String title   = "";
    private String content = "";

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public String getToken(){
        return token;
    }

    public void setToken(String token){
        this.token = token;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getContent(){
        return content;
    }

    public void setContent(String content){
        this.content = content;
    }

    public Bitmap getBitmap(){
        Bitmap bt = null;

        ExpInfo expInfo = DataMgr.getInstance().getExpInfoByID(id);

        if(expInfo != null){
            // 先找原图
            List<String> localUrls = expInfo.getLocalUrls(false);
            bt = getBitmapImpl(localUrls);

            if(bt == null){
                // 原图不存在，找缩略图
                localUrls.clear();
                localUrls = expInfo.getLocalUrls(true);
                bt = getBitmapImpl(localUrls);
            }
        }

        // 如果都为空，返回默认图标
        if(bt == null){
            bt = BitmapFactory.decodeResource(MyApplication.getInstance().getResources(), R.drawable.logo);
        }

        return bt;

    }

    private Bitmap getBitmapImpl(List<String> localUrls){
        Bitmap bt = null;
        if(!localUrls.isEmpty()){
            String url = localUrls.get(0);

            try{
                bt = Utils.getLoacalBitmap(url, ConstantValue.NAIL_ICON_HEIGHT, ConstantValue.NAIL_ICON_WIDTH);
            } catch(Throwable e){
                e.printStackTrace();
            }
        }
        return bt;
    }

    @Override
    public String toString(){
        return "ShareToken [id=" + id + ", token=" + token + ", title=" + title + ", content=" + content + "]";
    }

    public String buildShareUrl(){
        String url = String.format(ServerUrls.EXP_SHARE_REAL_URL, token);
        url = url.replace("https", "http");
        return url;
    }
}
