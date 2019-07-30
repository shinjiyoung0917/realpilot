package com.example.realpilot.model.region;

import com.example.realpilot.excelModel.RegionData;
import com.example.realpilot.model.weather.DailyWeather;
import com.example.realpilot.model.weather.HourlyWeather;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class Sido extends Regions {
    /*private String uid;

    private String hCode;
    private String sidoName;
    private String createdDate;
    private Integer gridX;
    private Integer gridY;

    private List<Sigungu> sigungus = new ArrayList<>();
    private List<Eubmyeondong> eubmyeondongs = new ArrayList<>();
    private List<HourlyWeather> hourlyWeathers = new ArrayList<>();
    private List<DailyWeather> dailyWeathers = new ArrayList<>();*/

    public Sido setSido(Regions region) {
        this.setUid(region.getUid());
        this.setHCode(region.getHCode());
        this.setSidoName(region.getSidoName());
        this.setCreatedDate(region.getCreatedDate());

        // grid가 없는 경우도 있음
        Optional optinalValue = Optional.ofNullable(region.getGridX());
        if(optinalValue.isPresent()) {
            this.setGridX(region.getGridX());
            this.setGridY(region.getGridY());
        }

        return this;
    }

    /*public Sido setSido(RegionData regionData) {
        this.hCode = regionData.getHCode();
        this.sidoName = regionData.getSidoName();
        this.createdDate = regionData.getCreatedDate();

        // grid가 없는 경우도 있음
        Optional optinalValue = Optional.ofNullable(regionData.getGridX());
        if(optinalValue.isPresent()) {
            this.gridX = regionData.getGridX();
            this.gridY = regionData.getGridY();
        }

        return this;
    }*/
}
