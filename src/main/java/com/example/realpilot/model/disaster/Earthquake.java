package com.example.realpilot.model.disaster;

import com.example.realpilot.externalApiModel.earthquake.EarthquakeInfo;
import lombok.Data;

@Data
public class Earthquake extends Disasters {
    public void setEarthquake(String uid, EarthquakeInfo earthquakeInfo) {
        this.setUid(uid);
        this.setForecastType(earthquakeInfo.getFcTp());
        this.setReleaseDate(earthquakeInfo.getTmFc().substring(0, 8));
        this.setReleaseTime(earthquakeInfo.getTmFc().substring(8, 12));
        this.setOccurrenceDate(earthquakeInfo.getTmEqk().substring(0, 8));
        this.setOccurrenceTime(earthquakeInfo.getTmEqk().substring(8, 14));
        this.setEarthquakeLocation(earthquakeInfo.getLoc());
        this.setEarthquakeMagnitude(earthquakeInfo.getMt());
        this.setEarthquakeIntensity(earthquakeInfo.getInT());
        this.setEarthquakeDepth(earthquakeInfo.getDep());
        this.setEarthquakeImage(earthquakeInfo.getImg());
        this.setEarthquakeReference(earthquakeInfo.getRem());
    }
}
