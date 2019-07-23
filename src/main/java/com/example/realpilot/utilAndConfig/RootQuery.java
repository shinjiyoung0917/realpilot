package com.example.realpilot.utilAndConfig;

import lombok.Getter;
import lombok.Setter;

public enum  RootQuery {
    HOURLY_WEATHER_ROOT_QUERY("hourlyWeather"),
    DAILY_WEATHER_ROOT_QUERY("dailyWeather");

    @Getter
    @Setter
    private String rootQuery;

    RootQuery(String rootQuery) {
        this.rootQuery = rootQuery;
    }
}
