package com.djc.logintest.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.baidu.android.pushservice.PushConstants;
import com.djc.logintest.constant.ConstantValue;
import com.djc.logintest.constant.JSONConstant;
import com.djc.logintest.push.info.JsonHelper;
import com.djc.logintest.push.info.PushEvent;
import com.djc.logintest.utils.Utils;

public class PushEventHandler {
    private static final String TAGS_SEPERATOR = ",";
    private static final int MAX_THREAD_IN_POOL = 100;
    private static PushEventHandler handler = new PushEventHandler();
    private ScheduledExecutorService genericService = Executors
            .newScheduledThreadPool(MAX_THREAD_IN_POOL);
    private boolean start = false;
    private List<PushEventObserver> observers = new ArrayList<PushEventObserver>();
    private BlockingQueue<PushEvent> eventQueue = new ArrayBlockingQueue<PushEvent>(
            ConstantValue.PUSH_ACTION_QUEUE_MAX_SIZE);

    private PushEventHandler() {
    }

    public static PushEventHandler getPushEventHandler() {
        return handler;
    }

    public void offerEvent(PushEvent e) {
        Log.d("bbind", "offerEvent e:" + e.toString());
        eventQueue.offer(e);
    }

    public void addObserver(PushEventObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(PushEventObserver observer) {
        observers.remove(observer);
    }

    public void clearObservers() {
        observers.clear();
    }

    public void start() {
        if (!start) {
            start = true;
            genericService.execute(new Runnable() {
                @Override
                public void run() {
                    while (!genericService.isShutdown()) {
                        try {
                            PushEvent event = eventQueue.take();
                            if (!handle(event)) {
                                notifyObservers(event);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    protected boolean handle(PushEvent event) {
        Log.d("DJC 10-16", "handle event:" + event);
        boolean bret = false;
        try {
            Log.d("bbind", "handle PushConstants.METHOD_BIND=" + PushConstants.METHOD_BIND);
            Log.d("bbind", "handle method=" + event.getMethod());
            Log.d("bbind", "handle errorcode=" + event.getErrorCode());
            if (PushConstants.METHOD_BIND.equals(event.getMethod()) && event.getErrorCode() == 0) {
                // 收到百度服务器发送的push绑定成功消息,保存数据
                JsonHelper.saveBindInfo(event.getMessage());
                bret = true;
            } else if (PushConstants.METHOD_SET_TAGS.equals(event.getMethod())
                    && event.getErrorCode() == 0) {
                // 绑定tag成功,保存数据
                Utils.savePushProp(JSONConstant.PUSH_TAGS, getTags(event));
                bret = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bret;
    }

    public String getTags(PushEvent event) {
        String tags = "";
        String message = event.getMessage();
        try {
            JSONObject jsonObject = new JSONObject(message);
            String params = jsonObject.getString(JSONConstant.RESPONSE_PARAMS);
            JSONObject tagsDetail = new JSONObject(params);
            String detail = tagsDetail.getString(JSONConstant.TAGS_DETAIL);
            JSONArray tagsArray = new JSONArray(detail);
            for (int i = 0; i < tagsArray.length(); i++) {
                JSONObject tag = tagsArray.getJSONObject(i);
                int result = tag.getInt(JSONConstant.TAGS_RESULT);
                if (result == 0) {
                    tags += tag.getString(JSONConstant.TAG) + TAGS_SEPERATOR;
                }
            }
            if (tags.endsWith(TAGS_SEPERATOR)) {
                tags = tags.substring(0, tags.length() - 1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tags;
    }

    protected void notifyObservers(PushEvent action) {
        for (PushEventObserver observer : observers) {
            observer.handle(action);
        }
    }

    public void clear() {
        eventQueue.clear();
        observers.clear();
    }

}
