package com.example.realpilot.service;

import com.example.realpilot.dao.RegionDao;
import com.example.realpilot.dgraph.DgraphOperations;
import com.example.realpilot.externalApiModel.forecastGrib.ForecastGrib;
import com.example.realpilot.externalApiModel.weatherWarning.OpenModel;
import com.example.realpilot.externalApiModel.weatherWarning.WeatherWarning;
import com.example.realpilot.model.region.Region;
import com.example.realpilot.model.region.RegionQueryResult;
import com.example.realpilot.model.weather.HourlyWeather;
import com.example.realpilot.utilAndConfig.WxMappingJackson2HttpMessageConverter;
import com.google.gson.Gson;
import io.dgraph.DgraphClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Set;

@Service
public class WeatherService {
    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    @Autowired
    private RegionService regionService;
    @Autowired
    private RegionDao regionDao;

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

           // 격자로 해당 노드들 전부 조회
           List<Region> regionByGrid = regionDao.getRegionNodeByGrid(gridX, gridY);

//           URI uri = URI.create(forecastGribApiUrl + "?ServiceKey=" + serviceKey + "&base_date=" +   + "&base_time=" +  + "&nx=" + gridX + "&ny=" + gridY + "&_type=json");
           URI uri = URI.create(forecastGribApiUrl + "?ServiceKey=" + serviceKey + "&base_date=20190712"  + "&base_time=0600" + "&nx=" + gridX + "&ny=" + gridY + "&_type=json");

           try {
               openModel = restTemplate.getForObject(uri, com.example.realpilot.externalApiModel.forecastGrib.OpenModel.class);
           } catch (Exception e) {
               e.printStackTrace();
           }


           // 3중포문 될 것 같은데,,,,,
           for(Region region : regionByGrid) {


           }

           // 날짜 노드 조회


           // 조회한 지역 객체와 날짜 객체에 날씨 노드 연결
           for(ForecastGrib forecastGrib : openModel.getResponse().getBody().getItems().getItem()) {
               HourlyWeather hourlyWeather = new HourlyWeather();

               log.info("[Service] callWeatherApiByGrid - category : " + forecastGrib.getCategory());
               log.info("[Service] callWeatherApiByGrid - value : " + forecastGrib.getObsrValue());

               hourlyWeather.setHourlyWeather(forecastGrib);

               // set으로 지역, 날짜 객체 수정(날씨 객체 추가)

           }
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
