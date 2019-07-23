package com.example.realpilot.model.weather;

import lombok.Data;

@Data
public class DailyWeather extends Weathers {
    public void setDailyWeather(String uid, String tm, String icon, String wtext, Integer mintemp, Integer maxtemp, Integer rainp) {
        this.setUid(uid);
        this.setTm(tm);
        this.setIcon(icon);
        this.setWtext(wtext);
        this.setMintemp(mintemp);
        this.setMaxtemp(maxtemp);
        this.setRainp(rainp);
    }
}
