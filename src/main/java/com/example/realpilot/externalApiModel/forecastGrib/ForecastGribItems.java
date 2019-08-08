package com.example.realpilot.externalApiModel.forecastGrib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastGribItems {
    private List<ForecastGrib> item;
}
