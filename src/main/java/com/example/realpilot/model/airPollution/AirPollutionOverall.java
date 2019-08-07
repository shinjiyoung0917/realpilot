package com.example.realpilot.model.airPollution;

import com.example.realpilot.externalApiModel.airPollutionForecastOverall.AirPollutionForecastOverall;
import lombok.Data;

@Data
public class AirPollutionOverall extends AirPollutions {
    public void setAirPollutionOverall(String uid, AirPollutionForecastOverall airPollutionForecastOverall, int dayTerm) {
        this.setUid(uid);
        this.setForecastDate(airPollutionForecastOverall.getInformData().replaceAll("-", ""));
        if(dayTerm == 0) {
            String[] releaseDateArray = airPollutionForecastOverall.getDataTime().split(" ", 2);
            this.setForecastTime(releaseDateArray[1].substring(0, 2) + "00");
        }
        this.setAirPollutionCode(airPollutionForecastOverall.getInformCode());
        this.setAirPollutionOverall(airPollutionForecastOverall.getInformOverall());
        this.setAirPollutionCause(airPollutionForecastOverall.getInformCause());
        this.setAirPollutionGrade(airPollutionForecastOverall.getInformGrade());
    }
}
