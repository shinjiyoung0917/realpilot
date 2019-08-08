package com.example.realpilot.externalApiModel.earthquake;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EarthquakeInfoBody {
    private EarthquakeInfoItems items;
}
