package com.example.realpilot.model.region;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Region {
    private List<Sido> sidos = new ArrayList<>();

    private String uid;
    private String hCode;
    private String sidoName;
    private String sggName;
    private String umdName;
    private String createdDate;
    private Integer gridX;
    private Integer gridY;
    private double tmX;
    private double tmY;

    public void setRegion() {
    }
}
