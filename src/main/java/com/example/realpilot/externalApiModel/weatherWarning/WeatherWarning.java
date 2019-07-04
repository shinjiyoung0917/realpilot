package com.example.realpilot.externalApiModel.weatherWarning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherWarning {
    private String t1;
    private String t2;
    private String t3;

    @Override
    public String toString() {
        return "City{" +
                "t1=" + t1 +
                ", " +
                "t2=" + t2 +
                ", " +
                "t3='" + t3 + '\'' +
                '}';
    }

}
