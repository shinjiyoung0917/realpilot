package com.example.realpilot.service;

import com.example.realpilot.utilAndConfig.WxMappingJackson2HttpMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class TotalService {
    private static final Logger log = LoggerFactory.getLogger(TotalService.class);

    @Autowired
    private RegionService regionService;
    @Autowired
    private DateService dateService;
    @Autowired
    private WeatherService weatherService;
    @Autowired
    private AirPollutionService airPollutionService;
    @Autowired
    private DisasterService disasterService;

    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    private void totalFlow() throws IOException {
        /*regionService.doForAddressCodeFile();
        regionService.doForGridFile();

        regionService.addRegionNode();
        regionService.printRegionData();
        dateService.addDateNode();
*/
        restTemplate.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());

        //regionService.callTmCoordinateApi();
        //airPollutionService.callNearbyMeasureStationListApi();
        //airPollutionService.callRealTimeAirPollutionApi();
        //airPollutionService.callAirPollutionForecastApi();
        //weatherService.callSpecialWeatherApi();
        disasterService.callEarthquakeApi();

        //weatherService.callWeatherApiOfKma();
        weatherService.callWeatherApiOfKweather();
    }

}
