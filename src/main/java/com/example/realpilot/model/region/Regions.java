package com.example.realpilot.model.region;

import com.example.realpilot.model.weather.DailyWeather;
import com.example.realpilot.model.weather.HourlyWeather;
import com.example.realpilot.utilAndConfig.RegionUnit;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class Regions {
    private String uid;

    private String hCode;
    private String sidoName;
    private String sggName;
    private String umdName;
    private String createdDate;
    private Integer gridX;
    private Integer gridY;
    private Double tmX;
    private Double tmY;

    private List<Sido> sidos = new ArrayList<>(); // 연속적으로 한번에 삽입할 때 사용
    private List<Sigungu> sigungus;
    private List<Eubmyeondong> eubmyeondongs;
    private List<HourlyWeather> hourlyWeathers = new ArrayList<>();
    private List<DailyWeather> dailyWeathers = new ArrayList<>();

    public void setRegionUidAndName(Regions region,  RegionUnit regionUnit) {
        switch (regionUnit) {
            case SIDO:
                this.uid = region.getUid();
                this.sidoName = region.getSidoName();
                break;
            case SIDO_SGG:
                Optional.ofNullable(region.getSigungus()).ifPresent((sigungus) -> this.uid = sigungus.get(0).getUid());
                Optional.ofNullable(region.getSigungus()).ifPresent((sigungus) -> this.sggName = sigungus.get(0).getSggName());
                break;
            case SIDO_SGG_UMD:
                if(Optional.ofNullable(region.getSigungus()).isPresent()) {
                    List<Eubmyeondong> eubmyeondongs = region.getSigungus().get(0).getEubmyeondongs();
                    if(!eubmyeondongs.isEmpty()) {
                        this.uid = eubmyeondongs.get(0).getUid();
                        this.umdName = eubmyeondongs.get(0).getUmdName();
                    }
                }
                break;
            case SIDO_UMD:
                Optional.ofNullable(region.getEubmyeondongs()).ifPresent((eubmyeondongs) -> this.uid = eubmyeondongs.get(0).getUid());
                Optional.ofNullable(region.getEubmyeondongs()).ifPresent((eubmyeondongs) -> this.umdName = eubmyeondongs.get(0).getUmdName());
                break;
        }
    }
}
