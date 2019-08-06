package com.example.realpilot.model.airPollution;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AirPollutions {
    private String uid;

    private String date;
    private String time;
    private Float ozoneValue;
    private Float fineDustValue;
    private Float ultraFineDustValue;
    private Integer ozoneGrade;
    private Integer fineDustGrade;
    private Integer ultraFineDustGrade;
    private Integer fineDustGradeFor1h;
    private Integer ultraFineDustGradeFor1h;

    private String airPollutionCode;
    private String airPollutionOverall;
    private String airPollutionCause;
    private String airPollutionGrade;
    private String actionKnack;

    private List<AirPollutionDetail> airPollutionDetails = new ArrayList<>();
    private List<AirPollutionOverall> airPollutionOveralls = new ArrayList<>();
}
