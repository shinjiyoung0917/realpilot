package com.example.realpilot.externalApiModel.weatherWarning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Items {
    private List<WeatherWarning> item;

    @Override
    public String toString() {
        return "Items{" +
                " item='" + item.toString() + '\'' +
                '}';
    }
}
