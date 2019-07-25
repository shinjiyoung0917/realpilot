package com.example.realpilot.model.weather;

import com.example.realpilot.externalApiModel.kweatherDay7.DayOfDay7;
import lombok.Data;

@Data
public class DailyWeather extends Weathers {
    public void setDailyWeather(String uid, DayOfDay7 day) {
        this.setUid(uid);
        this.setTm(day.getTm());
        this.setIcon(day.getIcon());
        this.setWtext(day.getWtext());
        this.setMintemp(day.getMintemp());
        this.setMaxtemp(day.getMaxtemp());
        this.setRainp(day.getRainp());
    }
}
