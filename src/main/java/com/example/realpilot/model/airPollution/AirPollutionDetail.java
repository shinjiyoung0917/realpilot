package com.example.realpilot.model.airPollution;

import lombok.Data;

@Data
public class AirPollutionDetail {
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
