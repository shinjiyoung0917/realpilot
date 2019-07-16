package com.example.realpilot.externalApiModel.forecastGrib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastGrib {
    private String baseDate;
    private String baseTime;
    private String category;
    private Float obsrValue;
}
