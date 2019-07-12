package com.example.realpilot.model.weather;

import com.example.realpilot.externalApiModel.forecastGrib.ForecastGrib;
import lombok.Data;

@Data
public class HourlyWeather {
    private String category;
    private Integer obsrValue;

    public void setHourlyWeather(ForecastGrib forecastGrib) {
        this.category = forecastGrib.getCategory();
        this.obsrValue = forecastGrib.getObsrValue();
    }
}
