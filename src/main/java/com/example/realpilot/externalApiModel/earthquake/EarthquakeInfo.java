package com.example.realpilot.externalApiModel.earthquake;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EarthquakeInfo {
    private Integer fcTp;
    private String tmFc;
    private String tmEqk;
    private String loc;
    private String mt; //Float
    private String inT;
    private String dep;
    private String img;
    private String rem;
}
