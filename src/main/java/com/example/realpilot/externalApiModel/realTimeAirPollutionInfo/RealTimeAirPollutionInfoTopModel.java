package com.example.realpilot.externalApiModel.realTimeAirPollutionInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RealTimeAirPollutionInfoTopModel {
    List<RealTimeAirPollutionInfo> list;
}
