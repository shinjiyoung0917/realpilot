package com.example.realpilot.model.region;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class RegionRootQuery {
    private List<Regions> region;
    private List<Country> country;
    private Set<Regions> grid;
    private Set<Regions> tmCoordinate;
    private Set<Regions> measureStationInfo;
}
