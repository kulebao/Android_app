package com.djc.logintest.bean;

public class ScheduleListItem {
    private String dayofweek = "";
    private String date = "";
    private String morningContent = "";
    private String afternoonContent = "";

    public String getDayofweek() {
        return dayofweek;
    }

    public void setDayofweek(String dayofweek) {
        this.dayofweek = dayofweek;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMorningContent() {
        return morningContent;
    }

    public void setMorningContent(String morningContent) {
        this.morningContent = morningContent;
    }

    public String getAfternoonContent() {
        return afternoonContent;
    }

    public void setAfternoonContent(String afternoonContent) {
        this.afternoonContent = afternoonContent;
    }

}
