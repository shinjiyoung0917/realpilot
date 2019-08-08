package com.example.realpilot.externalApiModel.forecastSpace;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForecastSpaceBody {
    private ForecastSpaceItems items;
    private int totalCount;
}
