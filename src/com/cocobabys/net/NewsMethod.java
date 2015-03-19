package com.cocobabys.net;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.constant.JSONConstant;
import com.cocobabys.constant.ServerUrls;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.InfoHelper;
import com.cocobabys.dbmgr.info.News;
import com.cocobabys.dbmgr.info.ReceiptInfo;
import com.cocobabys.httpclientmgr.HttpClientHelper;
import com.cocobabys.utils.MethodUtils;

public class NewsMethod{

    private static final String MOST = "most";
    private static final String FROM = "from";
    private static final String TO   = "to";

    private NewsMethod(){}

    public static NewsMethod getMethod(){
        return new NewsMethod();
    }

    public List<News> getNormalNews(int most, long from, long to) throws Exception{
        List<News> list = new ArrayList<News>();
        HttpResult result = new HttpResult();
        String command = createNormalNoticeCommand(most, from, to);
        Log.e("DDDDD ", "getSchoolInfo cmd:" + command);
        result = HttpClientHelper.executeGet(command);
        list = handleGetNormalNewsResult(result);
        return list;
    }

    public MethodResult sendFeedBack(int newsid) throws Exception{
        MethodResult methodResult = new MethodResult(EventType.POST_RECEIPT_FAIL);
        HttpResult result = new HttpResult();
        String url = createSendFeedBack(newsid);
        String parentInfo = InfoHelper.getParentInfo();
        Log.e("DDDDD ", "createSendFeedBack cmd:" + url + " content=" + parentInfo);
        result = HttpClientHelper.executePost(url, parentInfo);
        if(result.getResCode() == HttpStatus.SC_OK){
            int errcode = result.getJsonObject().getInt(JSONConstant.ERROR_CODE);
            if(errcode == 0){
                ReceiptInfo info = new ReceiptInfo();
                info.setReceipt_id(newsid);
                info.setReceipt_state(1);
                // newsid 作为index，add的时候replace，所以直接add就可以了
                DataMgr.getInstance().addReceiptInfo(info);
                methodResult.setResultType(EventType.POST_RECEIPT_SUCCESS);
            }
        }
        return methodResult;
    }

    private String createSendFeedBack(int newsid){
        return String.format(ServerUrls.POST_NEWS_RECEIPT, DataMgr.getInstance().getSchoolID(), newsid);
    }

    private List<News> handleGetNormalNewsResult(HttpResult result) throws JSONException{
        List<News> list = new ArrayList<News>();
        if(result.getResCode() == HttpStatus.SC_OK){
            JSONArray array = result.getJSONArray();

            for(int i = 0; i < array.length(); i++){
                JSONObject object = array.getJSONObject(i);
                News info = parseNews(object);
                // NoticePaserHelper.setNormalParams(object, notice);
                list.add(info);
            }
        }

        return list;
    }

    private News parseNews(JSONObject object) throws JSONException{
        News info = new News();
        String title = object.getString("title");
        long timestamp = object.getLong(JSONConstant.TIME_STAMP);
        String body = object.getString("content");
        int serverID = object.getInt("news_id");
        int class_id = object.getInt("class_id");
        String image = object.getString("image");
        boolean needReceipt = object.getBoolean("feedback_required");

        info.setClass_id(class_id);
        info.setContent(body);
        info.setNews_server_id(serverID);
        // info.setPublisher("测试djc");
        info.setIcon_url(image);
        info.setTitle(title);
        info.setTimestamp(timestamp);
        info.setType(JSONConstant.NOTICE_TYPE_NORMAL);
        info.setNeed_receipt(needReceipt ? 1 : 0);
        return info;
    }

    private String createNormalNoticeCommand(int most, long from, long to){
        if(most == 0){
            most = ConstantValue.GET_NORMAL_NOTICE_MAX_COUNT;
        }
        String cmd = String.format(ServerUrls.GET_NORMAL_NOTICE, DataMgr.getInstance().getSchoolID());

        cmd += MOST + "=" + most;
        if(from != 0){
            cmd += "&" + FROM + "=" + from;
        }

        if(to != 0){
            cmd += "&" + TO + "=" + to;
        }

        cmd += "&class_id=" + MethodUtils.getAllFormatedClassid();

        Log.d("DDD", "createNormalNoticeCommand cmd=" + cmd);
        return cmd;
    }

}
