package com.example.realpilot.model.weather;

import com.example.realpilot.externalApiModel.specialWeather.SpecialWeatherReport;
import lombok.Data;

@Data
public class SpecialWeather extends Weathers {
    public void setSpecialWeather(String uid, SpecialWeatherReport specialWeatherReport) {
        this.setUid(uid);
        this.setReleaseDate(specialWeatherReport.getTmFc().substring(0, 8));
        this.setReleaseTime(specialWeatherReport.getTmFc().substring(8, 12));
        this.setAreaOfSpecialWeather(specialWeatherReport.getT2());
        this.setCurrentStatusSpecialWeather(specialWeatherReport.getT6());
    }

}
