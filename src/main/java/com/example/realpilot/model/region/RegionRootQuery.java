package com.example.realpilot.model.region;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class RegionRootQuery {
    private List<Regions> region;
    private List<Regions> regionByUid;
    private Set<Regions> gridXY;
    private Set<Regions> tmXY;
}
