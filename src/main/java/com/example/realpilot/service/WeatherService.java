package com.example.realpilot.service;

import com.example.realpilot.dao.DateDao;
import com.example.realpilot.dao.RegionDao;
import com.example.realpilot.dao.WeatherDao;
import com.example.realpilot.externalApiModel.forecastGrib.ForecastGrib;
import com.example.realpilot.externalApiModel.weatherWarning.OpenModel;
import com.example.realpilot.externalApiModel.weatherWarning.WeatherWarning;
import com.example.realpilot.model.date.Dates;
import com.example.realpilot.model.date.Hour;
import com.example.realpilot.model.region.Regions;
import com.example.realpilot.model.weather.HourlyWeather;
import com.example.realpilot.model.weather.Weathers;
import com.example.realpilot.utilAndConfig.ExternalWeatherApi;
import com.example.realpilot.utilAndConfig.WxMappingJackson2HttpMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;

@Service
public class WeatherService {
    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    @Autowired
    private RegionService regionService;
    @Autowired
    private DateService dateService;
    @Autowired
    private RegionDao regionDao;
    @Autowired
    private WeatherDao weatherDao;
    @Autowired
    private DateDao dateDao;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${forecastGrib.api.url}")
    private String forecastGribApiUrl;
    @Value("${weatherWarning.api.url}")
    private String weatherWarningApiUrl;
    @Value("${api.serviceKey}")
    private String serviceKey;

    private Integer GRID_X_IDNEX = 0;
    private Integer GRID_Y_INDEX = 1;

    // 동네예보 API(초단기실황/초단기예보/동네예보)
    public void callWeatherApiByGrid() {
        Set<List<Integer>> gridSet = regionService.gridSet;

        com.example.realpilot.externalApiModel.forecastGrib.OpenModel openModel = new com.example.realpilot.externalApiModel.forecastGrib.OpenModel();
       for(List<Integer> grid : gridSet) {
           Integer gridX = grid.get(GRID_X_IDNEX);
           Integer gridY = grid.get(GRID_Y_INDEX);

           List<Regions> regionByGrid = regionDao.getRegionNodeByGrid(gridX, gridY);

           // API 호출은 초단기실황의 경우에 40분 이후, 초단기예보의 경우에 45분 이후에 호출하도록
           String baseDate = dateService.makeBaseDateFormat();
           String baseTime = dateService.makeBaseTimeFormat(ExternalWeatherApi.FORECAST_GRIB);

           //URI uri = URI.create(forecastGribApiUrl + "?ServiceKey=" + serviceKey + "&base_date=" + baseDate + "&base_time=" + baseTime + "&nx=" + gridX + "&ny=" + gridY + "&_type=json");
           URI uri = URI.create(forecastGribApiUrl + "?ServiceKey=" + serviceKey + "&base_date=" + baseDate + "&base_time=1000" + "&nx=" + gridX + "&ny=" + gridY + "&_type=json");

           try {
               openModel = restTemplate.getForObject(uri, com.example.realpilot.externalApiModel.forecastGrib.OpenModel.class);
           } catch (Exception e) {
               e.printStackTrace();
           }

           HourlyWeather hourlyWeather = new HourlyWeather();
           List<String> categories = new ArrayList<>();
           List<Float> obsrValues = new ArrayList<>();

           for (ForecastGrib forecastGrib : openModel.getResponse().getBody().getItems().getItem()) {
               log.info("[Service] callWeatherApiByGrid - category : " + forecastGrib.getCategory());
               log.info("[Service] callWeatherApiByGrid - value : " + forecastGrib.getObsrValue());

               categories.add(forecastGrib.getCategory());
               obsrValues.add(forecastGrib.getObsrValue());
           }
           hourlyWeather.setHourlyWeather(categories, obsrValues, baseDate, baseTime);

           Hour hour = dateDao.getCurrentTimeNode();
           for(Regions region : regionByGrid) {
               region.getHourlyWeathers().add(hourlyWeather);
               regionDao.updateRegionNode(region);

               // region의 uid로 날씨 노드 조회
               HourlyWeather foundHourlyWeather = weatherDao.getHourlyWeatherNode(region.getUid(), baseDate, baseTime);

               // 날짜 객체 수정 (날씨 객체 연결)
               hour.getHourlyWeathers().add(foundHourlyWeather);
           }
           dateDao.updateDateNode(hour);
           log.info("[Service] callWeatherApiByGrid - " + hour.getHour() + "시의 " + "지역->날씨<-날짜 노드 연결 완료");
       }
    }

    public void callWeatherWarningApi() {
        restTemplate.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());

        URI uri = URI.create(weatherWarningApiUrl + "?ServiceKey=" + serviceKey + "&fromTmFc=20190528&toTmFc=20190704&_type=json");

        OpenModel openModel = restTemplate.getForObject(uri, OpenModel.class);
        log.info("[Service] callWeatherWarningApi - : " + openModel);

        for(WeatherWarning weatherWarning : openModel.getResponse().getBody().getItems().getItem()) {
            log.info("[Service] callWeatherWarningApi - t1 : " + weatherWarning.getT1());
            log.info("[Service] callWeatherWarningApi - t2 : " + weatherWarning.getT2());
            log.info("[Service] callWeatherWarningApi - t3 : " + weatherWarning.getT3());
        }
    }
}
