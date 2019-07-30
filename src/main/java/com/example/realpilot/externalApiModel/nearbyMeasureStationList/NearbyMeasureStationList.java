package com.example.realpilot.externalApiModel.nearbyMeasureStationList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NearbyMeasureStationList {
    private String stationName;
    private String addr;
    private Float tm;
}
