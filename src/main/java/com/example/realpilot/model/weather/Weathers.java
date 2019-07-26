package com.example.realpilot.model.weather;

import com.example.realpilot.model.date.Month;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Weathers {
    private String uid;

    private String baseDate;
    private String baseTime;
    private String fcstDate;
    private String fcstTime;

    // TODO: 이름 변경
    private Float POP;
    private Float PTY;
    private Float R06;
    private Float REH;
    private Float S06;
    private Float SKY;
    private Float T3H;
    private Float TMN;
    private Float TMX;
    private Float UUU;
    private Float VVV;
    private Float WAV;
    private Float VEC;
    private Float WSD;

    private Float T1H;
    private Float RN1;

    private Float LGT;

    private String tm;
    private Integer tfh;
    private String tmyo;
    private String icon;
    private String wtext;
    private Integer mintemp;
    private Integer maxtemp;
    private Integer rainp;

    private String amIcon;
    private String pmIcon;
    private String amWtext;
    private String pmWtext;
    private Integer amMintemp;
    private Integer pmMintemp;
    private Integer amMaxtemp;
    private Integer pmMaxtemp;
    private Integer amRainp;
    private Integer pmRainp;

    private String icon40a;
    private String temp;

    private String sidoName;
    private String sggName;
    private String umdName;

    private Integer year;
    private List<Month> months;

    private List<HourlyWeather> hourlyWeathers = new ArrayList<>();
    private List<DailyWeather> dailyWeathers = new ArrayList<>();
    private List<AmWeather> amWeathers = new ArrayList<>();
    private List<PmWeather> pmWeathers = new ArrayList<>();

}
