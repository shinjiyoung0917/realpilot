package com.example.realpilot.externalApiModel.airPollutionForecastOverall;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AirPollutionForecastOverall {
    private String dataTime;
    private String informData;
    private String informCode;
    private String informOverall;
    private String informCause;
    private String informGrade;
    private String actionKnack;
}
