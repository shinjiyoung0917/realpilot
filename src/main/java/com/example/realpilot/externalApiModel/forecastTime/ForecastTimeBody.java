package com.example.realpilot.externalApiModel.forecastTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastTimeBody {
    private ForecastTimeItems items;
    private int totalCount;
}
