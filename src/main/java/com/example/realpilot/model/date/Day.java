package com.example.realpilot.model.date;

import com.example.realpilot.model.airPollution.AirPollutionOverall;
import com.example.realpilot.model.weather.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class Day { // TODO: extends Dates
    private String uid;

    private Integer day;

    private List<Hour> hours = new ArrayList<>();

    private List<HourlyWeather> hourlyWeathers = new ArrayList<>();
    private List<DailyWeather> dailyWeathers = new ArrayList<>();
    private List<AmWeather> amWeathers = new ArrayList<>();
    private List<PmWeather> pmWeathers = new ArrayList<>();
    private List<WorldDailyWeather> worldDailyWeathers = new ArrayList<>();
    private List<AirPollutionOverall> airPollutionOveralls = new ArrayList<>();

    public void setDate(int d, List<Hour> hourList) {
        this.day = d;
        this.hours = hourList;
    }
}
