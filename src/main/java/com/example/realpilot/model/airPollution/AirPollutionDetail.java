package com.example.realpilot.model.airPollution;

import com.example.realpilot.externalApiModel.realTimeAirPollutionInfo.RealTimeAirPollutionInfo;
import lombok.Data;

@Data
public class AirPollutionDetail extends AirPollutions {
    public void setAirPollutionDetail(String uid, RealTimeAirPollutionInfo airPollutionInfo) {
        this.setUid(uid);
        String dateAndTime = airPollutionInfo.getDataTime().replaceAll("-|:", "");
        this.setForecastDate(dateAndTime.substring(0, 8));
        this.setForecastTime(dateAndTime.substring(9, 13));
        if(!airPollutionInfo.getO3Value().equals("-") && !airPollutionInfo.getO3Value().equals("")) {
            this.setOzoneValue(Float.parseFloat(airPollutionInfo.getO3Value()));
        }
        if(!airPollutionInfo.getPm10Value().equals("-") && !airPollutionInfo.getPm10Value().equals("")) {
            this.setFineDustValue(Float.parseFloat(airPollutionInfo.getPm10Value()));
        }
        if(!airPollutionInfo.getPm25Value().equals("-") && !airPollutionInfo.getPm25Value().equals("")) {
            this.setUltraFineDustValue(Float.parseFloat(airPollutionInfo.getPm25Value()));
        }
        if(!airPollutionInfo.getO3Grade().equals("-") && !airPollutionInfo.getO3Grade().equals("")) {
            this.setOzoneGrade(Integer.parseInt(airPollutionInfo.getO3Grade()));
        }
        if(!airPollutionInfo.getPm10Grade().equals("-") && !airPollutionInfo.getPm10Grade().equals("")) {
            this.setFineDustGrade(Integer.parseInt(airPollutionInfo.getPm10Grade()));
        }
        if(!airPollutionInfo.getPm25Grade().equals("-") && !airPollutionInfo.getPm25Grade().equals("")) {
            this.setUltraFineDustGrade(Integer.parseInt(airPollutionInfo.getPm25Grade()));
        }
        if(!airPollutionInfo.getPm10Grade1h().equals("-") && !airPollutionInfo.getPm10Grade1h().equals("")) {
            this.setFineDustGradeFor1h(Integer.parseInt(airPollutionInfo.getPm10Grade1h()));
        }
        if(!airPollutionInfo.getPm25Grade1h().equals("-") && !airPollutionInfo.getPm25Grade1h().equals("")) {
            this.setUltraFineDustGradeFor1h(Integer.parseInt(airPollutionInfo.getPm25Grade1h()));
        }
    }
}