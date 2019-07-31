package com.example.realpilot.externalApiModel.realTimeAirPollutionInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RealTimeAirPollutionInfo {
    private String date;
    private String time;
    private Float o3Value;
    private Float pm10Value;
    private Float pm25Value;
    private Integer o3Grade;
    private Integer pm10Grade;
    private Integer pm25Grade;
    private Integer pm10Grade1h;
    private Integer pm25Grade1h;
}
