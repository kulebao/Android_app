package com.cocobabys.dbmgr.info;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.text.TextUtils;

import com.cocobabys.R;
import com.cocobabys.activities.MyApplication;
import com.cocobabys.bean.CookbookItem;
import com.cocobabys.utils.Utils;

public class CookBookInfo {
    public static final String ID = "_id";
    public static final String COOKBOOK_ID = "cookbook_id";
    public static final String COOKBOOK_CONTENT = "cookbook_content";
    private String cookbook_id = "";

    private String cookbook_content = "";
    private String timestamp = "";
    private int id = 0;

    public String getCookbook_id() {
        return cookbook_id;
    }

    public void setCookbook_id(String cookbook_id) {
        this.cookbook_id = cookbook_id;
    }

    public String getCookbook_content() {
        return cookbook_content;
    }

    public void setCookbook_content(String cookbook_content) {
        this.cookbook_content = cookbook_content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<CookbookItem> getCookbookItemList() {
        Resources resources = MyApplication.getInstance().getResources();
        List<CookbookItem> list = new ArrayList<CookbookItem>();
        if (!TextUtils.isEmpty(cookbook_content)) {
            try {
                JSONObject object = new JSONObject(cookbook_content);

                // 计算出本周星期一的日历时间
                Calendar cal = Utils.getMonDayCalendar();

                String oneday = getDetail(object, InfoHelper.MON);
                if (!TextUtils.isEmpty(oneday)) {
                    CookbookItem item = getItem(oneday);
                    item.setCookWeek(resources.getString(R.string.mon));
                    item.setCookDate(InfoHelper.getYearMonthDayFormat().format(cal.getTime()));
                    list.add(item);
                }
                
                oneday = getDetail(object, InfoHelper.TUE);
                if (!TextUtils.isEmpty(oneday)) {
                    CookbookItem item = getItem(oneday);
                    item.setCookWeek(resources.getString(R.string.tue));
                    cal.add(Calendar.DATE, 1);
                    item.setCookDate(InfoHelper.getYearMonthDayFormat().format(cal.getTime()));
                    list.add(item);
                }
                
                oneday = getDetail(object, InfoHelper.WED);
                if (!TextUtils.isEmpty(oneday)) {
                    CookbookItem item = getItem(oneday);
                    item.setCookWeek(resources.getString(R.string.wed));
                    cal.add(Calendar.DATE, 1);
                    item.setCookDate(InfoHelper.getYearMonthDayFormat().format(cal.getTime()));
                    list.add(item);
                }
                
                oneday = getDetail(object, InfoHelper.THU);
                if (!TextUtils.isEmpty(oneday)) {
                    CookbookItem item = getItem(oneday);
                    item.setCookWeek(resources.getString(R.string.thu));
                    cal.add(Calendar.DATE, 1);
                    item.setCookDate(InfoHelper.getYearMonthDayFormat().format(cal.getTime()));
                    list.add(item);
                }
                
                oneday = getDetail(object, InfoHelper.FRI);
                if (!TextUtils.isEmpty(oneday)) {
                    CookbookItem item = getItem(oneday);
                    item.setCookWeek(resources.getString(R.string.fri));
                    cal.add(Calendar.DATE, 1);
                    item.setCookDate(InfoHelper.getYearMonthDayFormat().format(cal.getTime()));
                    list.add(item);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private CookbookItem getItem(String oneday) throws JSONException {
        CookbookItem item = new CookbookItem();
        JSONObject detail = new JSONObject(oneday);
        item.setFirstContent(detail.getString("breakfast"));
        item.setSecContent(detail.getString("lunch"));
        item.setThirdContent(detail.getString("extra"));
        item.setFouthContent(detail.getString("dinner"));
        return item;
    }

    public String getDetail(JSONObject object, String key) {
        String oneday = "";
        try {
            oneday = object.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return oneday;
    }
}
