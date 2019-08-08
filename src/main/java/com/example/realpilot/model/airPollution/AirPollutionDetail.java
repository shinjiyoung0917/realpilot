package com.example.realpilot.model.airPollution;

import com.example.realpilot.externalApiModel.realTimeAirPollution.RealTimeAirPollution;
import lombok.Data;

@Data
public class AirPollutionDetail extends AirPollutions {
    public void setAirPollutionDetail(String uid, RealTimeAirPollution realTimeAirPollution) {
        this.setUid(uid);
        String dateAndTime = realTimeAirPollution.getDataTime().replaceAll("-|:", "");
        this.setForecastDate(dateAndTime.substring(0, 8));
        this.setForecastTime(dateAndTime.substring(9, 13));
        if(!realTimeAirPollution.getO3Value().equals("-") && !realTimeAirPollution.getO3Value().equals("")) {
            this.setOzoneValue(Float.parseFloat(realTimeAirPollution.getO3Value()));
        }
        if(!realTimeAirPollution.getPm10Value().equals("-") && !realTimeAirPollution.getPm10Value().equals("")) {
            this.setFineDustValue(Float.parseFloat(realTimeAirPollution.getPm10Value()));
        }
        if(!realTimeAirPollution.getPm25Value().equals("-") && !realTimeAirPollution.getPm25Value().equals("")) {
            this.setUltraFineDustValue(Float.parseFloat(realTimeAirPollution.getPm25Value()));
        }
        if(!realTimeAirPollution.getO3Grade().equals("-") && !realTimeAirPollution.getO3Grade().equals("")) {
            this.setOzoneGrade(Integer.parseInt(realTimeAirPollution.getO3Grade()));
        }
        if(!realTimeAirPollution.getPm10Grade().equals("-") && !realTimeAirPollution.getPm10Grade().equals("")) {
            this.setFineDustGrade(Integer.parseInt(realTimeAirPollution.getPm10Grade()));
        }
        if(!realTimeAirPollution.getPm25Grade().equals("-") && !realTimeAirPollution.getPm25Grade().equals("")) {
            this.setUltraFineDustGrade(Integer.parseInt(realTimeAirPollution.getPm25Grade()));
        }
        if(!realTimeAirPollution.getPm10Grade1h().equals("-") && !realTimeAirPollution.getPm10Grade1h().equals("")) {
            this.setFineDustGradeFor1h(Integer.parseInt(realTimeAirPollution.getPm10Grade1h()));
        }
        if(!realTimeAirPollution.getPm25Grade1h().equals("-") && !realTimeAirPollution.getPm25Grade1h().equals("")) {
            this.setUltraFineDustGradeFor1h(Integer.parseInt(realTimeAirPollution.getPm25Grade1h()));
        }
    }
}