package com.example.realpilot.model.region;

import com.example.realpilot.excelModel.RegionData;
import com.example.realpilot.externalApiModel.nearbyMeasureStationList.NearbyMeasureStationList;
import com.example.realpilot.externalApiModel.tmCoordinate.TmCoordinate;
import com.example.realpilot.model.weather.DailyWeather;
import com.example.realpilot.model.weather.HourlyWeather;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class Eubmyeondong<T> extends Regions {
    /*private String uid;

    private String hCode;
    private String umdName;
    private String createdDate;
    private Integer gridX;
    private Integer gridY;
    private Float tmX;
    private Float tmY;

    private List<HourlyWeather> hourlyWeathers = new ArrayList<>();
    private List<DailyWeather> dailyWeathers = new ArrayList<>();*/

    public Eubmyeondong setEubmyeondong(Sido sido, Regions region) {
        setEubmyeondong(region);
        sido.getEubmyeondongs().add(this);

        return this;
    }

    public Eubmyeondong setEubmyeondong(Sigungu sigungu, Regions region) {
        setEubmyeondong(region);
        sigungu.getEubmyeondongs().add(this);

        return this;
    }

    private void setEubmyeondong(Regions region) {
        this.setUid(region.getUid());
        this.setHCode(region.getHCode());
        this.setUmdName(region.getUmdName());
        this.setCreatedDate(region.getCreatedDate());

        // grid가 없는 경우도 있음
        Optional optinalValue = Optional.ofNullable(region.getGridX());
        if(optinalValue.isPresent()) {
            this.setGridX(region.getGridX());
            this.setGridY(region.getGridY());
        }

        optinalValue = Optional.ofNullable(region.getTmX());
        if(optinalValue.isPresent()) {
            this.setTmX(region.getTmX());
            this.setTmY(region.getTmY());
        }
    }

    public void setEubmyeondongByTmCoord(TmCoordinate tm) {
        this.setTmX(tm.getTmX());
        this.setTmY(tm.getTmY());
    }

    public void setEubmyeondongByMeasureSation(NearbyMeasureStationList measureStation) {
        this.setMeasureStationName(measureStation.getStationName());
        this.setMeasureStationAddr(measureStation.getAddr());
    }

 /*   public void setEubmyeondong(Regions region) {
        this.setUid(region.getUid());
        this.setHCode(region.getHCode());
        this.setUmdName(region.getUmdName());
        this.setCreatedDate(region.getCreatedDate());
        this.setGridX(region.getGridX());
        this.setGridY(region.getGridY());
        this.setTmX(region.getTmX());
        this.setTmY(region.getTmY());
    }*/



    /*private void setRegionData(RegionData regionData) {
        this.hCode = regionData.getHCode();
        this.umdName = regionData.getUmdName();
        this.createdDate = regionData.getCreatedDate();

        // grid가 없는 경우도 있음
        Optional optinalValue = Optional.ofNullable(regionData.getGridX());
        if(optinalValue.isPresent()) {
            this.gridX = regionData.getGridX();
            this.gridY = regionData.getGridY();
        }

        optinalValue = Optional.ofNullable(regionData.getTmX());
        if(optinalValue.isPresent()) {
            this.tmX = regionData.getTmX();
            this.tmY = regionData.getTmY();
        }
    }*/


}
