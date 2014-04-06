package com.cocobabys.noticepaser;

import org.json.JSONObject;

import com.cocobabys.dbmgr.info.Notice;

public interface NoticePaser {
    public Notice saveData(JSONObject object);

}
