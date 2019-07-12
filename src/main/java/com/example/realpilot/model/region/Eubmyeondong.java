package com.example.realpilot.model.region;

import com.example.realpilot.excelModel.RegionData;
import com.example.realpilot.model.weather.HourlyWeather;
import lombok.Data;

import java.util.List;
import java.util.Optional;

@Data
public class Eubmyeondong {
    private String uid;
    private String hCode;
    private String umdName;
    private String createdDate;
    private Integer gridX;
    private Integer gridY;
    private double tmX;
    private double tmY;
    private List<HourlyWeather> hourlyWeather;

    public Eubmyeondong setRegion(Sigungu sggObject, RegionData regionData) {
        this.hCode = regionData.getHCode();
        this.umdName = regionData.getUmdName();
        this.createdDate = regionData.getCreatedDate();

        // grid가 없는 경우도 있음
        Optional optinalValue = Optional.ofNullable(regionData.getGridX());
        if(optinalValue.isPresent()) {
            this.gridX = regionData.getGridX();
            this.gridY = regionData.getGridY();
        }

        //optinalValue = Optional.ofNullable(regionData.getTmX());
        if(regionData.getTmX() != 0 && regionData.getTmY() != 0) {
            this.tmX = regionData.getTmX();
            this.tmY = regionData.getTmY();
        }

        sggObject.getEubmyeondongs().add(this);

        return this;
    }

}
