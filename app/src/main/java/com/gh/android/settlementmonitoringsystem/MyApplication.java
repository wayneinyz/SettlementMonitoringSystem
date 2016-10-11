package com.gh.android.settlementmonitoringsystem;

import android.app.Application;

import java.util.Calendar;
import java.util.Date;

public class MyApplication extends Application {

    private String dateRef;
    public String dateCol;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // 获取当前系统时间的前一天的17:00:00
    public static Date getNextDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        date = calendar.getTime();
        return date;
    }

    public void setRefDate(String dateRef) {
        this.dateRef = dateRef;
    }

    public String getRefDate() {
        return dateRef;
    }
    
    public void setColDate(String dateCol) {
        this.dateCol = dateCol;
    }

    public String getColDate() {
        return dateCol;
    }

}
