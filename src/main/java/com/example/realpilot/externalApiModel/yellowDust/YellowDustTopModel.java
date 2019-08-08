package com.example.realpilot.externalApiModel.yellowDust;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YellowDustTopModel {
    private List<YellowDust> list;
}
