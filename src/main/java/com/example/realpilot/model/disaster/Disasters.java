package com.example.realpilot.model.disaster;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Disasters {
    private String uid;

    private Integer forecastType;
    private String releaseDate;
    private String releaseTime;
    private String occurrenceDate;
    private String occurrenceTime;
    private String earthquakeImage;
    private String earthquakeLocation;
    private String earthquakeMagnitude;
    private String earthquakeIntensity;
    private String earthquakeDepth;
    private String earthquakeReference;

    private List<Earthquake> earthquakes = new ArrayList<>();
}
