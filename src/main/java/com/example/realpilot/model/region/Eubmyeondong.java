package com.example.realpilot.model.region;

import com.example.realpilot.externalApiModel.tmCoordinate.TmCoordinate;

import lombok.Data;

import java.util.Optional;

@Data
public class Eubmyeondong<T> extends Regions {
    /*private String uid;

    private String hCode;
    private String umdName;
    private String createdDate;
    private Integer gridX;
    private Integer gridY;
    private Double tmX;
    private Double tmY;

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

    public void setEubmyeondongByTmCoord(TmCoordinate tmCoordinate) {
        this.setTmX(tmCoordinate.getTmX());
        this.setTmY(tmCoordinate.getTmY());
    }
}
