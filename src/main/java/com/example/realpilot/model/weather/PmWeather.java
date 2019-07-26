package com.example.realpilot.model.weather;

import com.example.realpilot.externalApiModel.kweatherAmPm7.DayOfAmPm7;

public class PmWeather extends Weathers {
    public void setPmWeather(String uid, DayOfAmPm7 day) {
        this.setUid(uid);
        this.setTm(day.getTm());
        this.setPmIcon(day.getPmicon());
        this.setPmWtext(day.getPmwtext());
        this.setPmMintemp(day.getPmmintemp());
        this.setPmMaxtemp(day.getPmmaxtemp());
        this.setPmRainp(day.getPmrainp());
    }
}
