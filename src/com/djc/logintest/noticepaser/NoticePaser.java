package com.djc.logintest.noticepaser;

import org.json.JSONObject;

import com.djc.logintest.dbmgr.info.Notice;

public interface NoticePaser {
    public Notice saveData(JSONObject object);

}
