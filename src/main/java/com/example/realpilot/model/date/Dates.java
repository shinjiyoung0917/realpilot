package com.example.realpilot.model.date;

import com.example.realpilot.model.weather.DailyWeather;
import com.example.realpilot.model.weather.HourlyWeather;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Dates {
    private String uid;

    private Integer year;
    private Integer month;
    private Integer day;
    private Integer hour;

    private List<Year> years = new ArrayList<>();
    private List<Month> months;
    private List<HourlyWeather> hourlyWeathers = new ArrayList<>();
    private List<DailyWeather> dailyWeathers = new ArrayList<>();
}
