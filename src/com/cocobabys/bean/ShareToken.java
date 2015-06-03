package com.cocobabys.bean;

import com.cocobabys.constant.ServerUrls;

public class ShareToken{
    private String token = "";

    public String getToken(){
        return token;
    }

    public void setToken(String token){
        this.token = token;
    }

    public String buildShareUrl(){
        return String.format(ServerUrls.EXP_SHARE_REAL_URL, token);
    }
}
