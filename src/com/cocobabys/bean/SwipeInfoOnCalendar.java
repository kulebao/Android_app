package com.cocobabys.bean;

import android.text.TextUtils;

public class SwipeInfoOnCalendar {
    private String checkin = "";
    private String checkout = "";

    public String getCheckin() {
        return checkin;
    }

    public void setCheckin(String checkin) {
        this.checkin = checkin;
    }

    public String getCheckout() {
        return checkout;
    }

    public void setCheckout(String checkout) {
        this.checkout = checkout;
    }

    public boolean isEmpty(){
        return TextUtils.isEmpty(checkin) && TextUtils.isEmpty(checkout);
    }
}
