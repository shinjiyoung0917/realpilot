package com.example.realpilot.model.weather;

import com.example.realpilot.model.date.Dates;
import com.example.realpilot.model.region.Regions;
import lombok.Data;

import java.util.List;

@Data
public class WeatherRootQuery {
    private List<HourlyWeather> hourlyWeather;
    private List<DailyWeather> dailyWeather;
    private List<AmWeather> amWeather;
    private List<PmWeather> pmWeather;
    private List<Regions> region;
    private List<Dates> date;
}
