package com.example.realpilot.externalApiModel.earthquake;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EarthquakeInfoItems {
    private List<EarthquakeInfo> item;
}
