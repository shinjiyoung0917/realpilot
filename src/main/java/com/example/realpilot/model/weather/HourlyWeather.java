package com.example.realpilot.model.weather;

import lombok.Data;

import java.util.Map;

@Data
public class HourlyWeather extends Weathers {
    public void setHourlyWeather(String uid, Map<String, Float> categoryValueMap, String baseDate, String baseTime) {
        this.setUid(uid);
        this.setBaseDate(baseDate);
        this.setBaseTime(baseTime);

        setCategoryValue(categoryValueMap);
    }

    public void setHourlyWeather(String uid, Map<String, Float> categoryValueMap, String baseDate, String baseTime, String fcstDate, String fcstTime) {
        this.setUid(uid);
        this.setBaseDate(baseDate);
        this.setBaseTime(baseTime);
        this.setFcstDate(fcstDate);
        this.setFcstTime(fcstTime);

        setCategoryValue(categoryValueMap);
    }

    public void setCategoryValue(Map<String, Float> categoryValueMap) {
        for(Map.Entry<String, Float> entry : categoryValueMap.entrySet()) {
            switch (entry.getKey()) {
                case "POP":
                    this.setPOP(entry.getValue());
                    break;
                case "PTY":
                    this.setPTY(entry.getValue());
                    break;
                case "R06":
                    this.setR06(entry.getValue());
                    break;
                case "REH":
                    this.setREH(entry.getValue());
                    break;
                case "S06":
                    this.setS06(entry.getValue());
                    break;
                case "SKY":
                    this.setSKY(entry.getValue());
                    break;
                case "T3H":
                    this.setT3H(entry.getValue());
                    break;
                case "TMN":
                    this.setTMN(entry.getValue());
                    break;
                case "TMX":
                    this.setTMX(entry.getValue());
                    break;
                case "UUU":
                    this.setUUU(entry.getValue());
                    break;
                case "VVV":
                    this.setVVV(entry.getValue());
                    break;
                case "WAV":
                    this.setWAV(entry.getValue());
                    break;
                case "VEC":
                    this.setVEC(entry.getValue());
                    break;
                case "WSD":
                    this.setWSD(entry.getValue());
                    break;
                case "T1H":
                    this.setT1H(entry.getValue());
                    break;
                case "RN1":
                    this.setRN1(entry.getValue());
                    break;
                case "LGT":
                    this.setLGT(entry.getValue());
                    break;
            }
        }
    }

    /*private Float POP;
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

    public void setHourlyWeather(List<String> categories, List<Float> obsrValues) {
        int listSize = categories.size();
        for(int i=0 ; i<listSize ; ++i) {
            switch (categories.get(i)) {
                case "POP":
                    this.POP = obsrValues.get(i);
                    break;
                case "PTY":
                    this.PTY = obsrValues.get(i);
                    break;
                case "R06":
                    this.R06 = obsrValues.get(i);
                    break;
                case "REH":
                    this.REH = obsrValues.get(i);
                    break;
                case "S06":
                    this.S06 = obsrValues.get(i);
                    break;
                case "SKY":
                    this.SKY = obsrValues.get(i);
                    break;
                case "T3H":
                    this.T3H = obsrValues.get(i);
                    break;
                case "TMN":
                    this.TMN = obsrValues.get(i);
                    break;
                case "TMX":
                    this.TMX = obsrValues.get(i);
                    break;
                case "UUU":
                    this.UUU = obsrValues.get(i);
                    break;
                case "VVV":
                    this.VVV = obsrValues.get(i);
                    break;
                case "WAV":
                    this.WAV = obsrValues.get(i);
                    break;
                case "VEC":
                    this.WAV = obsrValues.get(i);
                    break;
                case "WSD":
                    this.WSD = obsrValues.get(i);
                    break;
                case "T1H":
                    this.T1H = obsrValues.get(i);
                    break;
                case "RN1":
                    this.RN1 = obsrValues.get(i);
                    break;
                case "LGT":
                    this.LGT = obsrValues.get(i);
                    break;
            }
        }
    }*/
}
