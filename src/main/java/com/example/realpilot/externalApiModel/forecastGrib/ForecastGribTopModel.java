package com.example.realpilot.externalApiModel.forecastGrib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastGribTopModel {
    private ForecastGribResponse response;
}
