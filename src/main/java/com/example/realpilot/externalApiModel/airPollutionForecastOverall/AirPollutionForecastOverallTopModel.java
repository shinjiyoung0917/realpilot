package com.example.realpilot.externalApiModel.airPollutionForecastOverall;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AirPollutionForecastOverallTopModel {
    private List<AirPollutionForecastOverall> list;
}
