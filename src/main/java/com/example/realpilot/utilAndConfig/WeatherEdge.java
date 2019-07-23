package com.example.realpilot.utilAndConfig;

import lombok.Getter;
import lombok.Setter;

public enum  WeatherEdge {
    HOURLY_WEATHER("hourlyWeathers"),
    DAILY_WEATHER("dailyWeathers");

    @Getter
    @Setter
    private String weatherEdge;

    WeatherEdge(String weatherEdge) {
        this.weatherEdge = weatherEdge;
    }
}
