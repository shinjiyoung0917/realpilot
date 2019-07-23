package com.example.realpilot.model.date;

import com.example.realpilot.model.weather.DailyWeather;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Day {
    private String uid;

    private Integer day;

    private List<Hour> hours = new ArrayList<>();

    private List<DailyWeather> dailyWeathers = new ArrayList<>();

    public void setDate(int d, List<Hour> hourList) {
        this.day = d;
        this.hours = hourList;
    }
}
