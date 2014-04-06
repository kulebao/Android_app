package com.cocobabys.mycalendar;

public class MyCalendarFont {
    private int screenWidth = 480;

    public MyCalendarFont(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getCalendarDateFont() {
        if (screenWidth < 720) {
            return 24;
        } else { 
            return 32;
        }
    }
    
    public int getSwipeFont() {
        if (screenWidth < 720) {
            return 18;
        } else { 
            return 26;
        }
    }
}
