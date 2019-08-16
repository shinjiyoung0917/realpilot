package com.example.realpilot.service;

import com.example.realpilot.dgraph.DgraphConfig;
import com.example.realpilot.utilAndConfig.WxMappingJackson2HttpMessageConverter;
import io.dgraph.DgraphClient;
import io.dgraph.DgraphProto;
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
    private DgraphConfig dgraphConfig;
    @Autowired
    private DgraphClient dgraphClient;

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
        log.info(dgraphClient.toString());

        try {
            dgraphConfig.createSchema(dgraphClient);
            //createSchema(dgraphClient);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }

        /*regionService.loadRegionData();
        regionService.addKoreaRegionNode();
        regionService.printKoreaRegionData();
        regionService.addWorldRegionNode();
        dateService.addDateNode();
        */

        restTemplate.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());

        /*regionService.callTmCoordinateApi();
        airPollutionService.callNearbyMeasureStationListApi();*/

//        airPollutionService.callRealTimeAirPollutionApi();
//        airPollutionService.callAirPollutionForecastApi();
//        weatherService.callSpecialWeatherApi();
//        disasterService.callEarthquakeApi();

        //weatherService.callWeatherApiOfKma();
        weatherService.callWeatherApiOfKweather();
    }

    /*public void createSchema(DgraphClient dgraphClient) {
        // ** DB ALTER ** //
        // TODO: 필요할 때만 사용
        //dgraphClient.alter(DgraphProto.Operation.newBuilder().setDropAll(true).build());

        String schema = "year: int @index(int) .\n" +
                "month: int @index(int) .\n" +
                "day: int @index(int) .\n" +
                "hour: int @index(int) .\n" +
                "sidoName: string @index(fulltext, trigram) .\n" +
                "sggName: string @index(fulltext, trigram) .\n" +
                "umdName: string @index(fulltext, trigram) .\n" +
                "countryName: string @index(fulltext, trigram) .\n" +
                "hCode: int @index(int) .\n" +
                "createdDate: int @index(int) .\n" +
                "gridX: int @index(int) .\n" +
                "gridY: int @index(int) .\n" +
                "tmX: float @index(float) .\n" +
                "tmY: float @index(float) .\n" +
                "measureStationName: string @index(fulltext) .\n" +
                "airPollutionCode: string @index(fulltext) .\n" +
                "releaseDate: string @index(fulltext) .\n" +
                "releaseTime: string @index(fulltext) .\n" +
                "forecastDate: string @index(fulltext) .\n" +
                "forecastTime: string @index(fulltext) .\n" +
                "occurrenceDate: string @index(fulltext) .\n" +
                "occurrenceTime: string @index(fulltext) .\n" +
                "hourlyWeathers: uid @reverse .\n" +
                "dailyWeathers: uid @reverse .\n" +
                "amWeathers: uid @reverse .\n" +
                "pmWeathers: uid @reverse .\n" +
                "airPollutionDetails: uid @reverse .\n" +
                "airPollutionOveralls: uid @reverse .\n" +
                "worldDailyWeathers: uid @reverse .\n" +
                "earthquakes: uid @reverse .\n";

        DgraphProto.Operation op = DgraphProto.Operation.newBuilder().setSchema(schema).build();
        dgraphClient.alter(op);

        log.info("[DgraphConfig] createSchema - DGraph 스키마 세팅 완료");
    }*/
}
