package com.example.realpilot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @PostConstruct
    private void totalFlow() throws IOException {
        regionService.doForAddressCodeFile();
        regionService.doForGridFile();
        //regionService.callTmCoordinateApi();

        regionService.printRegionData();

        regionService.addRegionNode();
        dateService.addDateNode();

        //////weatherService.callWeatherApiByGrid();
        //weatherService.callWeatherWarningApi();
    }

}
