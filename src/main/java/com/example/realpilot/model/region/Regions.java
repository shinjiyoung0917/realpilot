package com.example.realpilot.model.region;

import com.example.realpilot.model.weather.AmWeather;
import com.example.realpilot.model.weather.DailyWeather;
import com.example.realpilot.model.weather.HourlyWeather;
import com.example.realpilot.model.weather.PmWeather;
import com.example.realpilot.utilAndConfig.RegionUnit;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private List<AmWeather> amWeathers = new ArrayList<>();
    private List<PmWeather> pmWeathers = new ArrayList<>();

    public void setRegionUidAndName(Regions region,  RegionUnit regionUnit) {
        switch (regionUnit) {
            case SIDO:
                this.uid = region.getUid();
                this.sidoName = region.getSidoName();
                break;
            case SIDO_SGG:
                List<Sigungu> sigunguList1 = region.getSigungus();
                if(Optional.ofNullable(sigunguList1).isPresent() && !sigunguList1.isEmpty()) {
                    this.uid = sigunguList1.get(0).getUid();
                    this.sggName = sigunguList1.get(0).getSggName();
                    this.sigungus = sigunguList1;
                }
                break;
            case SIDO_SGG_UMD:
                List<Sigungu> sigunguList2 = region.getSigungus();
                if(Optional.ofNullable(sigunguList2).isPresent()) {
                    List<Eubmyeondong> eubmyeondongs = sigunguList2.get(0).getEubmyeondongs();
                    if(Optional.ofNullable(eubmyeondongs).isPresent() && !eubmyeondongs.isEmpty()) {
                        this.uid = eubmyeondongs.get(0).getUid();
                        this.umdName = eubmyeondongs.get(0).getUmdName();
                        this.sigungus = sigunguList2;
                    }
                }
                break;
            case SIDO_UMD:
                List<Eubmyeondong> eubmyeondongList = region.getEubmyeondongs();
                if(Optional.ofNullable(eubmyeondongList).isPresent() && !eubmyeondongList.isEmpty()) {
                    this.uid = eubmyeondongList.get(0).getUid();
                    this.umdName = eubmyeondongList.get(0).getUmdName();
                    this.eubmyeondongs = eubmyeondongList;
                }
                break;
        }
    }
}
