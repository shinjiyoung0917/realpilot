package com.example.realpilot.externalApiModel.realTimeAirPollution;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RealTimeAirPollutionTopModel {
    private List<RealTimeAirPollution> list;
}
