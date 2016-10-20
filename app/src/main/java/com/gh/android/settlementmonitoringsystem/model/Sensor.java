package com.gh.android.settlementmonitoringsystem.model;

import java.io.Serializable;

public class Sensor implements Serializable {

    private String id;
    private double currentValue;
    private String updateAt;
    private double lastValue;
    private String lastTime;
    private float settleValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public String getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(String updateAt) {
        this.updateAt = updateAt;
    }

    public double getLastValue() {
        return lastValue;
    }

    public void setLastValue(double lastValue) {
        this.lastValue = lastValue;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public float getSettleValue() {
        return settleValue;
    }

    public void setSettleValue(float settleValue) {
        this.settleValue = settleValue;
    }
}
