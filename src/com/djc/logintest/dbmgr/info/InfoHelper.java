package com.djc.logintest.dbmgr.info;

import java.io.File;
import java.text.SimpleDateFormat;

import android.util.Log;

import com.djc.logintest.utils.Utils;

public class InfoHelper {
    private static final String BABY_ICON_NAME = "baby_icon";
    private static final String SCHOOL_LOGO = "school_logo";
    public static final String TIMESTAMP = "timestamp";
    public static final SimpleDateFormat YEAR_MONTH_DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat HOUR_MINUTE_FORMAT = new SimpleDateFormat("HH:mm");

    public static String getChildrenLocalIconPath(String childid) {
        String dir = Utils.getSDCardPicRootPath() + File.separator + BABY_ICON_NAME
                + File.separator;

        Utils.mkDirs(dir);
        String url = dir + childid;
        Log.d("DDD", "getChildrenDefaultLocalIconPath url=" + url);
        return url;
    }

    public static String getDefaultSchoolLocalIconPath() {
        String url = Utils.getSDCardPicRootPath() + File.separator + SCHOOL_LOGO;
        Log.d("DDD", "getChildrenDefaultLocalIconPath url=" + url);
        return url;
    }

    public static final String WEEK_DETAIL = "week";
    public static final String MON = "mon";
    public static final String TUE = "tue";
    public static final String WED = "wed";
    public static final String THU = "thu";
    public static final String FRI = "fri";
    public static final String TIME = "time";
    public static final String AM = "am";
    public static final String PM = "pm";

    // 学校信息最近更新时间
}
