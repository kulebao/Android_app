package com.djc.logintest.bean;

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
        return checkin.isEmpty() && checkout.isEmpty();
    }
}
