package com.example.realpilot.externalApiModel.forecastTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastTimeTopModel {
    private Response response;
}
