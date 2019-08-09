package com.example.realpilot.model.weather;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Weathers {
    private String uid;

    // TODO: HourlyWeather, DailyWeather, AmWeather, PmWeather에서 날짜, 시간 관련된 필드들 date, time으로 맞추기
    // TODO: 케이웨더와 동네예보 API 데이터에서 발표시각과 예보시간을 어떤 필드로 사용할지?
    /*private String baseDate;
    private String baseTime;
    private String fcstDate;
    private String fcstTime;*/
    private String releaseDate;
    private String releaseTime;
    private String forecastDate;
    private String forecastTime;

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

    private String tmyo;
    private String icon;
    private String wtext;
    private Float mintemp;
    private Float maxtemp;
    private Float rainp;

    private String amIcon;
    private String pmIcon;
    private String amWtext;
    private String pmWtext;
    private Float amMintemp;
    private Float pmMintemp;
    private Float amMaxtemp;
    private Float pmMaxtemp;
    private Float amRainp;
    private Float pmRainp;

    private Float temp;
    private String icon40a;

    private Float humidity;
    private Float windSpeed;

    private String areaOfSpecialWeather;
    private String currentStatusSpecialWeather;

    private List<HourlyWeather> hourlyWeathers = new ArrayList<>();
    private List<DailyWeather> dailyWeathers = new ArrayList<>();
    private List<AmWeather> amWeathers = new ArrayList<>();
    private List<PmWeather> pmWeathers = new ArrayList<>();
    private List<WorldDailyWeather> worldDailyWeathers = new ArrayList<>();
    private List<SpecialWeather> specialWeathers = new ArrayList<>();

}
