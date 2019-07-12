package com.example.realpilot.model.region;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
public class RegionQueryResult {
    // root query
    private List<Region> regionByGrid;
    private List<Region> regionByUid;
}
