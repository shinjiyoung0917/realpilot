package com.example.realpilot.model.airPollution;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AirPollutions {
    private String uid;

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

    List<AirPollutionDetail> airPollutionDetails = new ArrayList<>();
}
