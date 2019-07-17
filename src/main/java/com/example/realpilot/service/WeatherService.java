package com.example.realpilot.service;

import com.example.realpilot.dao.DateDao;
import com.example.realpilot.dao.RegionDao;
import com.example.realpilot.dao.WeatherDao;
import com.example.realpilot.externalApiModel.forecastGrib.ForecastGrib;
import com.example.realpilot.externalApiModel.forecastGrib.ForecastGribTopModel;
import com.example.realpilot.externalApiModel.forecastTime.ForecastTime;
import com.example.realpilot.externalApiModel.forecastTime.ForecastTimeTopModel;
import com.example.realpilot.externalApiModel.weatherWarning.WeatherWarningTopModel;
import com.example.realpilot.externalApiModel.weatherWarning.WeatherWarning;
import com.example.realpilot.model.date.Hour;
import com.example.realpilot.model.region.Regions;
import com.example.realpilot.model.weather.HourlyWeather;
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
    @Value("${forecastTime.api.url}")
    private String forecastTimeApiUrl;
    @Value("${weatherWarning.api.url}")
    private String weatherWarningApiUrl;
    @Value("${api.serviceKey}")
    private String serviceKey;

    private Integer GRID_X_IDNEX = 0;
    private Integer GRID_Y_INDEX = 1;

    // 동네예보 API(초단기실황/초단기예보/동네예보)
    public void callWeatherApiByGrid() {
        Set<List<Integer>> gridSet = regionService.gridSet;

        ForecastGribTopModel forecastGribTopModel = new ForecastGribTopModel();
        ForecastTimeTopModel forecastTimeTopModel = new ForecastTimeTopModel();

       for(List<Integer> grid : gridSet) {
           Integer gridX = grid.get(GRID_X_IDNEX);
           Integer gridY = grid.get(GRID_Y_INDEX);

           List<Regions> regionByGrid = regionDao.getRegionNodeByGrid(gridX, gridY);

           // API 호출은 초단기실황의 경우에 40분 이후, 초단기예보의 경우에 45분 이후에 호출하도록
           String baseDate = dateService.makeBaseDateFormat();
           String baseTime = "1000";
           //String baseTime = dateService.makeBaseTimeFormat(ExternalWeatherApi.FORECAST_GRIB);

           URI forecastGribUri = URI.create(forecastGribApiUrl + "?ServiceKey=" + serviceKey + "&base_date=" + baseDate + "&base_time=" + baseTime + "&nx=" + gridX + "&ny=" + gridY + "&numOfRows=300&_type=json");
           URI forecastTimeUri = URI.create(forecastTimeApiUrl + "?ServiceKey=" + serviceKey + "&base_date=" + baseDate + "&base_time=" + baseTime + "&nx=" + gridX + "&ny=" + gridY + "&numOfRows=300&_type=json");

           //callForecastGribApi(forecastGribTopModel, forecastGribUri, baseDate, baseTime, regionByGrid);
           callForecastTimeApi(forecastTimeTopModel, forecastTimeUri, baseDate, baseTime, regionByGrid);



       }
    }

    private void callForecastGribApi(ForecastGribTopModel forecastGribTopModel,URI forecastGribUri, String baseDate, String baseTime, List<Regions> regionByGrid) {
        try {
            forecastGribTopModel = restTemplate.getForObject(forecastGribUri, ForecastGribTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Float> categoryValueMap = new HashMap<>();

        // TODO: 반복문 돌면서 ~getItem 계속 호출되니 미리 정의해두고 사용하기
        for (ForecastGrib forecastGrib : forecastGribTopModel.getResponse().getBody().getItems().getItem()) {
            log.info("[Service] callForecastGribApi - category : " + forecastGrib.getCategory());
            log.info("[Service] callForecastGribApi - value : " + forecastGrib.getObsrValue());

            categoryValueMap.put(forecastGrib.getCategory(), forecastGrib.getObsrValue());
        }

        HourlyWeather hourlyWeather = new HourlyWeather();
        hourlyWeather.setHourlyWeather(categoryValueMap, baseDate, baseTime);

        // TODO : 날짜, 시간 얻어오는 로직 중복 해결하기
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        connectRegionAndWeatherAndDateNode(year, month, day, hour, baseDate, baseTime, regionByGrid, hourlyWeather, ExternalWeatherApi.FORECAST_GRIB);
    }

    private void callForecastTimeApi(ForecastTimeTopModel forecastTimeTopModel, URI forecastTimeUri, String baseDate, String baseTime, List<Regions> regionByGrid) {
        try {
            forecastTimeTopModel = restTemplate.getForObject(forecastTimeUri, ForecastTimeTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<HourlyWeather> hourlyWeatherList = new ArrayList<>();

        int forecastTimeCount = getForecastTimeCount(baseTime);
        int index = 0;

        Map<String, Float> map1 = new HashMap<>();
        Map<String, Float> map2 = new HashMap<>();
        Map<String, Float> map3 = new HashMap<>();
        Map<String, Float> map4 = new HashMap<>();
        String fcstDate = "";
        String fcstTime1 = "";
        String fcstTime2 = "";
        String fcstTime3 = "";
        String fcstTime4 = "";

        // TODO: 반복문 돌면서 ~getItem 계속 호출되니 미리 정의해두고 사용하기
        for (ForecastTime forecastTime : forecastTimeTopModel.getResponse().getBody().getItems().getItem()) {
            log.info("[Service] callForecastTimeApi - fcstTime : " + forecastTime.getFcstTime());
            log.info("[Service] callForecastTimeApi - category : " + forecastTime.getCategory());
            log.info("[Service] callForecastTimeApi - value : " + forecastTime.getFcstValue());

            fcstDate = forecastTime.getFcstDate();
            if(index % forecastTimeCount ==  0) {
                map1.put(forecastTime.getCategory(), forecastTime.getFcstValue());
                fcstTime1 = forecastTime.getFcstTime();
            } else if(index % forecastTimeCount ==  1) {
                map2.put(forecastTime.getCategory(), forecastTime.getFcstValue());
                fcstTime2 = forecastTime.getFcstTime();
            } else if(index % forecastTimeCount ==  2) {
                map3.put(forecastTime.getCategory(), forecastTime.getFcstValue());
                fcstTime3 = forecastTime.getFcstTime();
            } else if(index % forecastTimeCount ==  3) {
                map4.put(forecastTime.getCategory(), forecastTime.getFcstValue());
                fcstTime4 = forecastTime.getFcstTime();
            }
            ++index;
        }

        for(int i=0 ; i<forecastTimeCount ; ++i) {
            HourlyWeather hourlyWeather = new HourlyWeather();
            if(i == 0) {
                hourlyWeather.setHourlyWeather(map1, baseDate, baseTime, fcstDate, fcstTime1);
            } else  if(i == 1) {
                hourlyWeather.setHourlyWeather(map2, baseDate, baseTime, fcstDate, fcstTime2);
            } else  if(i == 2) {
                hourlyWeather.setHourlyWeather(map3, baseDate, baseTime, fcstDate, fcstTime3);
            } else  if(i == 3) {
                hourlyWeather.setHourlyWeather(map4, baseDate, baseTime, fcstDate, fcstTime4);
            }
            hourlyWeatherList.add(hourlyWeather);
        }

        for(HourlyWeather hourlyWeather : hourlyWeatherList) {
            // TODO : 날짜, 시간 얻어오는 로직 중복 해결하기
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String fcstTime = hourlyWeather.getFcstTime();
            Integer hour = Integer.parseInt(fcstTime.substring(0, 2));

            connectRegionAndWeatherAndDateNode(year, month, day, hour, fcstDate, fcstTime, regionByGrid, hourlyWeather, ExternalWeatherApi.FORECAST_TIME);
        }
    }

    private void connectRegionAndWeatherAndDateNode(int year, int month, int day, int hour, String date, String time, List<Regions> regionByGrid, HourlyWeather hourlyWeather, ExternalWeatherApi api) {
        Hour hourNode = dateDao.getDateNode(year, month, day, hour);
        HourlyWeather foundHourlyWeather = new HourlyWeather();

        for (Regions region : regionByGrid) {
            region.getHourlyWeathers().add(hourlyWeather);
            regionDao.updateRegionNode(region);
            region.getHourlyWeathers().clear();

            // region의 uid로 날씨 노드 조회
            if(api.equals(ExternalWeatherApi.FORECAST_GRIB)) {
                foundHourlyWeather = weatherDao.getHourlyWeatherNode(region.getUid(), date, time, ExternalWeatherApi.FORECAST_GRIB);
            } else if(api.equals(ExternalWeatherApi.FORECAST_TIME)) {
                foundHourlyWeather = weatherDao.getHourlyWeatherNode(region.getUid(), date, time, ExternalWeatherApi.FORECAST_TIME);
            }

            // TODO : 날씨-날짜 연결하는 것 같은데 DB에서 hour predicate 확인하면 hourlyWeathers predicate가 없음, 노드 더블클릭해서 확인해보면 연결은 되어있음(?)
            hourNode.getHourlyWeathers().add(foundHourlyWeather);
        }
        dateDao.updateDateNode(hourNode);

        log.info("[Service] connectRegionAndWeatherAndDateNode - " + hourNode.getHour() + "시의 " + "지역->날씨<-날짜 노드 연결 완료");
    }

    private int getForecastTimeCount(String baseTime) {
        int count = 0;
        Integer test = Integer.parseInt(baseTime);
        Integer time = Integer.parseInt(baseTime.substring(0, 2));
        if(time % 3 == 0) {
            count = 3;
        } else if(time % 3 == 1) {
            count = 2;
        } else if(time % 3 == 2) {
            count = 4;
        }

        return count;
    }

    public void callWeatherWarningApi() {
        restTemplate.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());

        URI uri = URI.create(weatherWarningApiUrl + "?ServiceKey=" + serviceKey + "&fromTmFc=20190528&toTmFc=20190704&_type=json");

        WeatherWarningTopModel weatherWarningTopModel = restTemplate.getForObject(uri, WeatherWarningTopModel.class);
        log.info("[Service] callWeatherWarningApi - : " + weatherWarningTopModel);

        for(WeatherWarning weatherWarning : weatherWarningTopModel.getResponse().getBody().getItems().getItem()) {
            log.info("[Service] callWeatherWarningApi - t1 : " + weatherWarning.getT1());
            log.info("[Service] callWeatherWarningApi - t2 : " + weatherWarning.getT2());
            log.info("[Service] callWeatherWarningApi - t3 : " + weatherWarning.getT3());
        }
    }
}
