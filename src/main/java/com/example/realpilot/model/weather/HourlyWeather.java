package com.example.realpilot.model.weather;

import com.example.realpilot.externalApiModel.kweatherShko.AreaOfShko;
import com.example.realpilot.service.DateService;
import com.example.realpilot.utilAndConfig.ExternalWeatherApi;
import lombok.Data;

import java.util.Map;

@Data
public class HourlyWeather extends Weathers {
    public void setHourlyWeather(String uid, Map<String, Float> categoryValueMap, String forecastDate, String forecastTime) {
        this.setUid(uid);
        this.setReleaseDate(forecastDate);
        this.setReleaseTime(forecastTime);
        this.setForecastDate(forecastDate);
        this.setForecastTime(forecastTime);

        setCategoryValue(categoryValueMap);
    }

    public void setHourlyWeather(String uid, Map<String, Float> categoryValueMap, String releaseDate, String releaseTime, String forecastDate, String forecastTime) {
        this.setUid(uid);
        this.setReleaseDate(releaseDate);
        this.setReleaseDate(releaseTime);
        this.setForecastDate(forecastDate);
        this.setForecastTime(forecastTime);

        setCategoryValue(categoryValueMap);
    }

    public void setHourlyWeather(String uid, AreaOfShko area) {
        this.setUid(uid);
        this.setForecastDate(area.getTm().replaceAll("/", ""));
        this.setWtext(area.getWtext());
        this.setTemp(area.getTemp());
        this.setIcon40a(area.getIcon40a());
        DateService dateService = new DateService();
        String currentTime = dateService.makeCurrentHourFormat(ExternalWeatherApi.KWEATHER_SHKO) + "00";
        this.setForecastTime(currentTime);
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
}
