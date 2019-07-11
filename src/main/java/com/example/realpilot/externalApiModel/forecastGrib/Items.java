package com.example.realpilot.externalApiModel.forecastGrib;

import com.example.realpilot.externalApiModel.weatherWarning.WeatherWarning;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Items {
    private List<WeatherWarning> item;
}
