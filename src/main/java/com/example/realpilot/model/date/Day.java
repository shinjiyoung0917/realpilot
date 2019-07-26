package com.example.realpilot.model.date;

import com.example.realpilot.model.weather.AmWeather;
import com.example.realpilot.model.weather.DailyWeather;
import com.example.realpilot.model.weather.PmWeather;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class Day {
    private String uid;

    private Integer day;

    private List<Hour> hours = new ArrayList<>();

    private List<DailyWeather> dailyWeathers = new ArrayList<>();
    private List<AmWeather> amWeathers = new ArrayList<>();
    private List<PmWeather> pmWeathers = new ArrayList<>();

    public void setDate(int d, List<Hour> hourList) {
        this.day = d;
        this.hours = hourList;
    }
}
