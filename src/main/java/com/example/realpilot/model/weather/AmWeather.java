package com.example.realpilot.model.weather;

import com.example.realpilot.externalApiModel.kweatherAmPm7.DayOfAmPm7;

public class AmWeather extends Weathers {
    public void setAmWeather(String uid, DayOfAmPm7 day) {
        this.setUid(uid);
        this.setTm(day.getTm());
        this.setAmIcon(day.getAmicon());
        this.setAmWtext(day.getAmwtext());
        this.setAmMintemp(day.getAmmintemp());
        this.setAmMaxtemp(day.getAmmaxtemp());
        this.setAmRainp(day.getAmrainp());
    }
}
