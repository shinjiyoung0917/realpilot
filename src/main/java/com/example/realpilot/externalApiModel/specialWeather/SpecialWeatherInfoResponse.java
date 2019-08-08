package com.example.realpilot.externalApiModel.specialWeather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpecialWeatherInfoResponse {
    private SpecialWeatherInfoBody body;
}
