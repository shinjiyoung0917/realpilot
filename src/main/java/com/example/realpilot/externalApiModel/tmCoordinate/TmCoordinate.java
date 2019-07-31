package com.example.realpilot.externalApiModel.tmCoordinate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmCoordinate {
    private String sidoName;
    private String sggName;
    private String umdName;
    private Float tmX;
    private Float tmY;
}
