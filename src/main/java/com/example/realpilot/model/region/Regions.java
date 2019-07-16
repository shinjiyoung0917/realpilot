package com.example.realpilot.model.region;

import com.example.realpilot.model.weather.HourlyWeather;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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
    private List<HourlyWeather> hourlyWeathers = new ArrayList<>();
}
