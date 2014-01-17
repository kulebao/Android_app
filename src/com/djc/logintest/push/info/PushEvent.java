package com.djc.logintest.push.info;

public class PushEvent {
    public static final int TYPE_MESSAGE = 1;
    public static final int TYPE_NOTIFICATION = 2;
    // 收到用户点击通知栏通知事件
    public static final int TYPE_NOTIFICATION_CLICK = 3;

    private int type = -1;

    private String method = "";
    private String message = "";
    private int errorCode = 0;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String content) {
        this.message = content;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "PushEvent [type=" + type + ", method=" + method + ", message=" + message
                + ", errorCode=" + errorCode + "]";
    }

}
