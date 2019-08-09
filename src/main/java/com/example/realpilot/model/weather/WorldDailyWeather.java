package com.example.realpilot.model.weather;

import com.example.realpilot.externalApiModel.kweatherWorld.DayOfWorld;
import lombok.Data;

@Data
public class WorldDailyWeather extends Weathers {
    public void setWorldDailyWeather(String uid, DayOfWorld day) {
        this.setUid(uid);
        this.setForecastDate(day.getTm().replaceAll("/", ""));
        this.setMintemp(day.getMintemp());
        this.setMaxtemp(day.getMaxtemp());
        this.setHumidity(day.getHumidity());
        this.setWindSpeed(day.getWindspeed());
        this.setWtext(day.getWtext());
        this.setIcon40a(day.getIcon40a());
    }
}
