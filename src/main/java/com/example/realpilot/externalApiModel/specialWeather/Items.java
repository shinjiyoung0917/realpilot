package com.example.realpilot.externalApiModel.specialWeather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Items {
    private List<SpecialWeatherReport> item;
}
