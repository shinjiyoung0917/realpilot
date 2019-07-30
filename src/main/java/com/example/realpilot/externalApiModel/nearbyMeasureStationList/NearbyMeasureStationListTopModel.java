package com.example.realpilot.externalApiModel.nearbyMeasureStationList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NearbyMeasureStationListTopModel {
    private List<NearbyMeasureStationList> list;
}
