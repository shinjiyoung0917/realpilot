package com.example.realpilot.externalApiModel.forecastSpace;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastSpaceTopModel {
    private ForecastSpaceResponse response;
}
