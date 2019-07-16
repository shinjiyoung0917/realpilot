package com.example.realpilot.model.region;

import lombok.Data;

import java.util.List;

@Data
public class RegionRootQuery {
    private List<Regions> regionByGrid;
    private List<Regions> regionByUid;
}
