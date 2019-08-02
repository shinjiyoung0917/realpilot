package com.example.realpilot.externalApiModel.realTimeAirPollutionInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RealTimeAirPollutionInfo {
    private String dataTime;
    private String mangName;
    private String o3Value;
    private String pm10Value;
    private String pm25Value;
    private String o3Grade;
    private String pm10Grade;
    private String pm25Grade;
    private String pm10Grade1h;
    private String pm25Grade1h;
}
