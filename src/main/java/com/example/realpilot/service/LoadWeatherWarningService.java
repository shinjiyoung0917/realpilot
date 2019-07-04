package com.example.realpilot.service;

import com.example.realpilot.externalApiModel.weatherWarning.OpenModel;
import com.example.realpilot.externalApiModel.weatherWarning.WeatherWarning;
import com.example.realpilot.utilAndConfig.WxMappingJackson2HttpMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URI;

@Service
public class LoadWeatherWarningService {
    private static final Logger log = LoggerFactory.getLogger(LoadExcelFileService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${weatherWarning.api.url}")
    private String weatherWarningApiUrl;

    public void callWeatherWarningApi() {
        restTemplate.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());

        String serviceKey = "y7hP75dT%2FAxfrX5WKcSg31EZIT%2BUXZPOaBXR%2F2xX9oyO1gKR1v5opJc0oX1VjhMB4s45r38hJgfhNfTtYJNyBg%3D%3D";
        URI uri = URI.create(weatherWarningApiUrl + "?ServiceKey=" + serviceKey + "&fromTmFc=20190528&toTmFc=20190704&_type=json");

        OpenModel openModel = restTemplate.getForObject(uri, OpenModel.class);
        log.info("callWeatherWarningApi - : " + openModel);

        for(WeatherWarning weatherWarning : openModel.getResponse().getBody().getItems().getItem()) {
            log.info("callWeatherWarningApi - t1 : " + weatherWarning.getT1());
            log.info("callWeatherWarningApi - t2 : " + weatherWarning.getT2());
            log.info("callWeatherWarningApi - t3 : " + weatherWarning.getT3());
        }
    }
}