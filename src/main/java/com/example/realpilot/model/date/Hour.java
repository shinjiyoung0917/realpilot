package com.example.realpilot.model.date;

import com.example.realpilot.model.weather.HourlyWeather;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Hour { // TODO: extends Dates
    private String uid;

    private Integer hour;

    private List<HourlyWeather> hourlyWeathers = new ArrayList<>();

    public void setDate(int h) {
        this.hour = h;
    }
}
