package com.example.realpilot.model.weather;

import lombok.Data;

import java.util.List;

@Data
public class WeatherRootQuery {
    private List<Weathers> hourlyWeather;
    private List<Weathers> dailyWeather;
}
