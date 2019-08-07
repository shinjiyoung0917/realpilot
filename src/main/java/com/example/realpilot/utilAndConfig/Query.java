package com.example.realpilot.utilAndConfig;

import lombok.Getter;
import lombok.Setter;

public enum Query {
    HOURLY_WEATHER("hourlyWeather", "hourlyWeathers"),
    DAILY_WEATHER("dailyWeather", "dailyWeathers"),
    AM_WEATHER("amWeather", "amWeathers"),
    PM_WEATHER("pmWeather", "pmWeathers"),
    AIR_POLLUTION_DETAIL("airPollutionDetail", "airPollutionDetails"),
    AIR_POLLUTION_OVERALL("airPollutionOverall", "airPollutionOveralls"),
    SPECIAL_WEATHER("specialWeather", "specialWeathers");

    @Getter
    @Setter
    private String rootQuery;
    @Getter
    @Setter
    private String edge;

    Query(String rootQuery, String edge) {
        this.rootQuery = rootQuery;
        this.edge = edge;
    }
}
