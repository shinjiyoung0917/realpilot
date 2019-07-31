package com.example.realpilot.model.airPollution;

import com.example.realpilot.model.date.Dates;
import com.example.realpilot.model.region.Regions;
import lombok.Data;

import java.util.List;

@Data
public class AirPollutionRootQuery {
    private List<AirPollutionDetail> airPollutionDetail;
    private List<Regions> region;
    private List<Dates> date;

    public void setAirPollutionRootQuery(AirPollutionRootQuery airPollutionRootQuery) {

    }
}
