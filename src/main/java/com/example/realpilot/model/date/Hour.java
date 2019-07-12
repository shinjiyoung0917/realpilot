package com.example.realpilot.model.date;

import com.example.realpilot.model.weather.HourlyWeather;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Hour implements Serializable {
    private String uid;
    private Integer hour;
    private List<HourlyWeather> hourlyWeather;
}
