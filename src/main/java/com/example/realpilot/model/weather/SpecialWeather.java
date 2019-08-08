package com.example.realpilot.model.weather;

import com.example.realpilot.externalApiModel.specialWeather.SpecialWeatherInfo;
import lombok.Data;

@Data
public class SpecialWeather extends Weathers {
    public void setSpecialWeather(String uid, SpecialWeatherInfo specialWeatherInfo) {
        this.setUid(uid);
        this.setReleaseDate(specialWeatherInfo.getTmFc().substring(0, 8));
        this.setReleaseTime(specialWeatherInfo.getTmFc().substring(8, 12));
        this.setAreaOfSpecialWeather(specialWeatherInfo.getT2());
        this.setCurrentStatusSpecialWeather(specialWeatherInfo.getT6());
    }

}
