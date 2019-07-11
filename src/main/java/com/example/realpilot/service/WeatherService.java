package com.example.realpilot.service;

import com.example.realpilot.dao.RegionDao;
import com.example.realpilot.dgraph.DgraphOperations;
import com.example.realpilot.externalApiModel.weatherWarning.OpenModel;
import com.example.realpilot.externalApiModel.weatherWarning.WeatherWarning;
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
public class WeatherService<T> {
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

    private Integer GRID_X_IDNEX;
    private Integer GRID_Y_INDEX;

    // 동네예보 API(초단기실황/초단기예보/동네예보)
    public void callWeatherApiByGrid() {
        //restTemplate.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());

        Set<List<Integer>> gridSet = regionService.gridSet;

       for(List<Integer> grid : gridSet) {
           Integer gridX = grid.get(GRID_X_IDNEX);
           Integer gridY = grid.get(GRID_Y_INDEX);

           // 격자로 해당 노드들 전부 조회
           regionDao.getRegionNodeByGrid(gridX, gridY);

           //URI uri = URI.create(forecastGribApiUrl + "?ServiceKey=" + serviceKey + "&base_date=" +  + "&base_time=" +  + "&_type=json");
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
