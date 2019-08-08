package com.example.realpilot.externalApiModel.earthquake;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EarthquakeInfoResponse {
    private EarthquakeInfoBody body;
}
