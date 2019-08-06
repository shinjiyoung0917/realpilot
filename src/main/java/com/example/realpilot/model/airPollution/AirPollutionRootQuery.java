package com.example.realpilot.model.airPollution;

import com.example.realpilot.model.date.Dates;
import com.example.realpilot.model.region.Regions;
import lombok.Data;

import java.util.List;

@Data
public class AirPollutionRootQuery {
    private List<Regions> region;
    private List<Dates> date;
    private List<AirPollutionDetail> airPollutionDetail;
    private List<AirPollutionOverall> airPollutionOverall;

    public void setAirPollutionRootQuery(AirPollutionRootQuery airPollutionRootQuery) {
        this.region = airPollutionRootQuery.getRegion();
        this.date = airPollutionRootQuery.getDate();
        this.airPollutionDetail = airPollutionRootQuery.getAirPollutionDetail();
        this.airPollutionOverall = airPollutionRootQuery.getAirPollutionOverall();
    }
}
