package com.example.realpilot.externalApiModel.forecastGrib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastGrib {
    private Integer baseDate;
    private String baseTime;
    private String category;
    private Integer obsrValue;
}
