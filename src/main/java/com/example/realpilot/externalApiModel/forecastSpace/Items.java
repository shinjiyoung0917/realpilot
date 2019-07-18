package com.example.realpilot.externalApiModel.forecastSpace;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Items {
    private List<ForecastSpace> item;
}
