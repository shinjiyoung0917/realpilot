package com.example.realpilot.model.disaster;

import com.example.realpilot.model.date.Dates;
import com.example.realpilot.model.region.Regions;
import lombok.Data;

import java.util.List;

@Data
public class DisasterRootQuery {
    private List<Earthquake> earthquake;
    private List<Regions> region;
    private List<Dates> date;

    public void setDisasterRootQuery(DisasterRootQuery disasterRootQuery) {
        this.earthquake = disasterRootQuery.getEarthquake();
        this.region = disasterRootQuery.getRegion();
        this.date = disasterRootQuery.getDate();
    }
}
