package com.example.realpilot.externalApiModel.specialWeather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpecialWeatherInfoItems {
    private List<SpecialWeatherInfo> item;
}
