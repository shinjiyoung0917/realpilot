package com.example.realpilot.externalApiModel.yellowDustInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YellowDustInfoTopModel {
    private List<YellowDustInfo> list;
}
