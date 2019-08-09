package com.example.realpilot.externalApiModel.kweatherWorld;

import lombok.Data;

@Data
public class DayOfWorld {
    private String city;
    private String tm;
    private Float mintemp;
    private Float maxtemp;
    private Float humidity;
    private Float windspeed;
    private String wtext;
    private String icon40a;
}
