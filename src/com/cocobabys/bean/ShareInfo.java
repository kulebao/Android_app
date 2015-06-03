package com.cocobabys.bean;

import com.cocobabys.constant.JSONConstant;

public class ShareInfo{
    // 分享的文字内容
    private String    title     = "";
    // 分享的文字内容
    private String    content   = "";

    // 本地源文件路径
    private String    localUrl  = "";
    // 分享出去的路径
    private String    mediaUrl  = "";
    // 分享的类型
    private String    mediaType = JSONConstant.IMAGE_TYPE;

    private ShareType shareType = ShareType.TYPE_NONE;

    public ShareType getShareType(){
        return shareType;
    }

    public void setShareType(ShareType shareType){
        this.shareType = shareType;
    }

    public ShareInfo(String content){
        this.content = content;
    }

    public ShareInfo(String content, String localUrl){
        this(content);
        this.localUrl = localUrl;
    }

    public ShareInfo(String content, String localUrl, String mediaType){
        this(content, localUrl);
        this.mediaType = mediaType;
    }

    public ShareInfo(String content, String localUrl, String mediaType, String mediaUrl){
        this(content, localUrl, mediaType);
        this.mediaUrl = mediaUrl;
    }

    public ShareInfo(){}

    public String getContent(){
        return content;
    }

    public void setContent(String content){
        this.content = content;
    }

    public String getLocalUrl(){
        return localUrl;
    }

    public void setLocalUrl(String localUrl){
        this.localUrl = localUrl;
    }

    public String getMediaUrl(){
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl){
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType(){
        return mediaType;
    }

    public void setMediaType(String mediaType){
        this.mediaType = mediaType;
    }

    public boolean isValidShareType(){
        return JSONConstant.IMAGE_TYPE.equals(mediaType) || JSONConstant.VIDEO_TYPE.equals(mediaType);
    }

    public static enum ShareType{
            TYPE_NONE,
            TYPE_FILE,
            TYPE_URL,
            TYPE_WEBPAGE
    }
}
