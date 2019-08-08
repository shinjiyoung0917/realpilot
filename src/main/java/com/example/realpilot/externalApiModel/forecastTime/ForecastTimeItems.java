package com.example.realpilot.externalApiModel.forecastTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastTimeItems {
    private List<ForecastTime> item;
}
