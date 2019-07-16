package com.example.realpilot.model.weather;

import lombok.Data;

import java.util.List;

@Data
public class HourlyWeather extends Weathers {
    public void setHourlyWeather(List<String> categories, List<Float> obsrValues, String baseDate, String baseTime) {
        this.setBaseDate(baseDate);
        this.setBaseTime(baseTime);

        int listSize = categories.size();
        for(int i=0 ; i<listSize ; ++i) {
            switch (categories.get(i)) {
                case "POP":
                    this.setPOP(obsrValues.get(i));
                    break;
                case "PTY":
                    this.setPTY(obsrValues.get(i));
                    break;
                case "R06":
                    this.setR06(obsrValues.get(i));
                    break;
                case "REH":
                    this.setREH(obsrValues.get(i));
                    break;
                case "S06":
                    this.setS06(obsrValues.get(i));
                    break;
                case "SKY":
                    this.setSKY(obsrValues.get(i));
                    break;
                case "T3H":
                    this.setT3H(obsrValues.get(i));
                    break;
                case "TMN":
                    this.setTMN(obsrValues.get(i));
                    break;
                case "TMX":
                    this.setTMX(obsrValues.get(i));
                    break;
                case "UUU":
                    this.setUUU(obsrValues.get(i));
                    break;
                case "VVV":
                    this.setVVV(obsrValues.get(i));
                    break;
                case "WAV":
                    this.setWAV(obsrValues.get(i));
                    break;
                case "VEC":
                    this.setVEC(obsrValues.get(i));
                    break;
                case "WSD":
                    this.setWSD(obsrValues.get(i));
                    break;
                case "T1H":
                    this.setT1H(obsrValues.get(i));
                    break;
                case "RN1":
                    this.setRN1(obsrValues.get(i));
                    break;
                case "LGT":
                    this.setLGT(obsrValues.get(i));
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
