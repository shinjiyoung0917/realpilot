package com.example.realpilot.service;

import com.example.realpilot.dao.DateDao;
import com.example.realpilot.dao.RegionDao;
import com.example.realpilot.dao.WeatherDao;
import com.example.realpilot.externalApiModel.forecastGrib.ForecastGrib;
import com.example.realpilot.externalApiModel.forecastGrib.ForecastGribTopModel;
import com.example.realpilot.externalApiModel.forecastSpace.ForecastSpace;
import com.example.realpilot.externalApiModel.forecastSpace.ForecastSpaceTopModel;
import com.example.realpilot.externalApiModel.forecastTime.ForecastTime;
import com.example.realpilot.externalApiModel.forecastTime.ForecastTimeTopModel;
import com.example.realpilot.externalApiModel.kweatherDay7.Area;
import com.example.realpilot.externalApiModel.kweatherDay7.Days;
import com.example.realpilot.externalApiModel.kweatherDay7.KweatherDay7TopModel;
import com.example.realpilot.externalApiModel.weatherWarning.WeatherWarningTopModel;
import com.example.realpilot.externalApiModel.weatherWarning.WeatherWarning;
import com.example.realpilot.model.date.Day;
import com.example.realpilot.model.date.Hour;
import com.example.realpilot.model.region.Regions;
import com.example.realpilot.model.weather.DailyWeather;
import com.example.realpilot.model.weather.HourlyWeather;
import com.example.realpilot.model.weather.WeatherRootQuery;
import com.example.realpilot.model.weather.Weathers;
import com.example.realpilot.utilAndConfig.DateUnit;
import com.example.realpilot.utilAndConfig.ExternalWeatherApi;
import com.example.realpilot.utilAndConfig.RegionUnit;
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
    @Value("${forecastSpace.api.url}")
    private String forecastSpaceApiUrl;
    @Value("${weatherWarning.api.url}")
    private String weatherWarningApiUrl;
    @Value("${api.serviceKey}")
    private String serviceKey;
    @Value("${kweatherDay7.api.url}")
    private String kweatherDay7ApiUrl;

    private Integer GRID_X_IDNEX = 0;
    private Integer GRID_Y_INDEX = 1;

    // 동네예보 API(초단기실황/초단기예보/동네예보)
    public void callWeatherApiByGrid() {
        Set<List<Integer>> gridSet = regionService.gridSet;

        ForecastGribTopModel forecastGribTopModel = new ForecastGribTopModel();
        ForecastTimeTopModel forecastTimeTopModel = new ForecastTimeTopModel();
        ForecastSpaceTopModel forecastSpaceTopModel = new ForecastSpaceTopModel();

       for(List<Integer> grid : gridSet) {
           Integer gridX = grid.get(GRID_X_IDNEX);
           Integer gridY = grid.get(GRID_Y_INDEX);

           List<Regions> regionByGrid = regionDao.getRegionNodeByGrid(gridX, gridY);

           String baseDate = dateService.makeBaseDateFormat();
           String baseTime = dateService.makeBaseTimeFormat(ExternalWeatherApi.FORECAST_GRIB);
           // TODO: 고정시킨 시간 값 지우기
           baseTime = "0800";

           String parameters = "?ServiceKey=" + serviceKey + "&base_date=" + baseDate + "&base_time=" + baseTime + "&nx=" + gridX + "&ny=" + gridY + "&numOfRows=300&_type=json";
           URI forecastGribUri = URI.create(forecastGribApiUrl + parameters);
           URI forecastTimeUri = URI.create(forecastTimeApiUrl + parameters);
           URI forecastSpaceUri = URI.create(forecastSpaceApiUrl + parameters);


           callForecastGribApi(forecastGribTopModel, forecastGribUri, baseDate, baseTime, regionByGrid);
           //callForecastTimeApi(forecastTimeTopModel, forecastTimeUri, baseDate, baseTime, regionByGrid);

           baseTime = dateService.makeBaseTimeFormat(ExternalWeatherApi.FORECAST_SPACE);
           // TODO: 고정시킨 시간 값 지우기
           baseTime = "0800";
           //callForecastSpaceApi(forecastSpaceTopModel, forecastSpaceUri, baseDate, baseTime, regionByGrid);
       }
    }

    public void callWeatherApiOfKweather() {
        KweatherDay7TopModel kweatherDay7TopModel = new KweatherDay7TopModel();

        URI kweatherDay7Uri = URI.create(kweatherDay7ApiUrl);

        callKweatherDay7Api(kweatherDay7TopModel, kweatherDay7Uri);
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
        hourlyWeather.setHourlyWeather(null, categoryValueMap, baseDate, baseTime);

        Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();

        connectRegionAndWeatherAndDateNode(dateMap, baseDate, baseTime, regionByGrid, hourlyWeather, ExternalWeatherApi.FORECAST_GRIB);
    }

    private void callForecastTimeApi(ForecastTimeTopModel forecastTimeTopModel, URI forecastTimeUri, String baseDate, String baseTime, List<Regions> regionByGrid) {
        try {
            forecastTimeTopModel = restTemplate.getForObject(forecastTimeUri, ForecastTimeTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int fcstTimeCount = getFcstTimeCount(baseTime); // 예보시간의 구간 수 (2개 or 3개 or 4개)
        int index = 0;

        Map<String, Float>[] categoryValueMapArray = new Map[4];
        for(int i=0 ; i < categoryValueMapArray.length ; ++i) {
            categoryValueMapArray[i] = new HashMap<>();
        }

        String fcstDate = "";
        String[] fcstTimeArray = new String[4];

        // TODO: 반복문 돌면서 ~getItem 계속 호출되니 미리 정의해두고 사용하기
        for (ForecastTime forecastTime : forecastTimeTopModel.getResponse().getBody().getItems().getItem()) {
            log.info("[Service] callForecastTimeApi - fcstTime : " + forecastTime.getFcstTime());
            log.info("[Service] callForecastTimeApi - category : " + forecastTime.getCategory());
            log.info("[Service] callForecastTimeApi - value : " + forecastTime.getFcstValue());

            fcstDate = forecastTime.getFcstDate();
            if(index % fcstTimeCount ==  0) {
                categoryValueMapArray[0].put(forecastTime.getCategory(), forecastTime.getFcstValue());
                fcstTimeArray[0] = forecastTime.getFcstTime();
            } else if(index % fcstTimeCount ==  1) {
                categoryValueMapArray[1].put(forecastTime.getCategory(), forecastTime.getFcstValue());
                fcstTimeArray[1] = forecastTime.getFcstTime();
            } else if(index % fcstTimeCount ==  2) {
                categoryValueMapArray[2].put(forecastTime.getCategory(), forecastTime.getFcstValue());
                fcstTimeArray[2] = forecastTime.getFcstTime();
            } else if(index % fcstTimeCount ==  3) {
                categoryValueMapArray[3].put(forecastTime.getCategory(), forecastTime.getFcstValue());
                fcstTimeArray[3] = forecastTime.getFcstTime();
            }
            ++index;
        }

        List<HourlyWeather> hourlyWeatherList = new ArrayList<>();

        for(int i=0 ; i < fcstTimeCount ; ++i) {
            HourlyWeather hourlyWeather = new HourlyWeather();
            if(i == 0) {
                hourlyWeather.setHourlyWeather(null, categoryValueMapArray[0], baseDate, baseTime, fcstDate, fcstTimeArray[0]);
            } else  if(i == 1) {
                hourlyWeather.setHourlyWeather(null, categoryValueMapArray[1], baseDate, baseTime, fcstDate, fcstTimeArray[1]);
            } else  if(i == 2) {
                hourlyWeather.setHourlyWeather(null, categoryValueMapArray[2], baseDate, baseTime, fcstDate, fcstTimeArray[2]);
            } else  if(i == 3) {
                hourlyWeather.setHourlyWeather(null, categoryValueMapArray[3], baseDate, baseTime, fcstDate, fcstTimeArray[3]);
            }
            hourlyWeatherList.add(hourlyWeather);
        }

        for(HourlyWeather hourlyWeather : hourlyWeatherList) {
            String fcstTime = hourlyWeather.getFcstTime();
            Map<DateUnit, Integer> dateMap = dateService.getFcstDate(fcstDate, fcstTime);

            connectRegionAndWeatherAndDateNode(dateMap, fcstDate, fcstTime, regionByGrid, hourlyWeather, ExternalWeatherApi.FORECAST_TIME);
        }
    }

    private void callForecastSpaceApi(ForecastSpaceTopModel forecastSpaceTopModel, URI forecastSpaceUri, String baseDate, String baseTime, List<Regions> regionByGrid) {
        try {
            forecastSpaceTopModel = restTemplate.getForObject(forecastSpaceUri, ForecastSpaceTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int index = 0;

        String prevFcstDate = "";
        String prevFcstTime = "";

        Map<String, Float> categoryValueMap = new HashMap<>();
        List<Map<String, Float>> categoryValueMapList = new ArrayList<>();
        List<HourlyWeather> hourlyWeatherList = new ArrayList<>();

        for(ForecastSpace forecastSpace : forecastSpaceTopModel.getResponse().getBody().getItems().getItem()) {
            HourlyWeather hourlyWeather = new HourlyWeather();

            if(!prevFcstTime.equals(forecastSpace.getFcstTime()) && index != 0) {
                hourlyWeather.setHourlyWeather(null, categoryValueMap, baseDate, baseTime, prevFcstDate, prevFcstTime);
                hourlyWeatherList.add(hourlyWeather);
                categoryValueMap = new HashMap<>();

            } else {
                categoryValueMap.put(forecastSpace.getCategory(), forecastSpace.getFcstValue());
                categoryValueMapList.add(categoryValueMap);
            }
            prevFcstDate = forecastSpace.getFcstDate();
            prevFcstTime = forecastSpace.getFcstTime();

            ++index;
        }

        for(HourlyWeather hourlyWeather : hourlyWeatherList) {
            String fcstDate = hourlyWeather.getFcstDate();
            String fcstTime = hourlyWeather.getFcstTime();

            Map<DateUnit, Integer> dateMap = dateService.getFcstDate(fcstDate, fcstTime);

            connectRegionAndWeatherAndDateNode(dateMap, fcstDate, fcstTime, regionByGrid, hourlyWeather, ExternalWeatherApi.FORECAST_SPACE);
        }
    }

    private void callKweatherDay7Api(KweatherDay7TopModel kweatherDay7TopModel, URI kweatherDay7Uri) {
        try {
            kweatherDay7TopModel = restTemplate.getForObject(kweatherDay7Uri, KweatherDay7TopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Area area : kweatherDay7TopModel.getAreas()) {
            Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();
            dateMap.remove(DateUnit.HOUR);
            Day dayNode = dateDao.getDayNode(dateMap);
            Integer currentDay = dateMap.get(DateUnit.DAY);

            String sidoName = "";
            String sggName = "";
            String umdName = "";

            WeatherRootQuery weatherRootQuery = null;

            if(area.getAreaname1().equals("세종특별자치시")) {
                sidoName = area.getAreaname1();
                umdName = area.getAreaname2();
                weatherRootQuery = weatherDao.getDailyWeatherNodeByRegionAndDate(sidoName, sggName, umdName, RegionUnit.SIDO_UMD);
            } else {
                if (area.getAreaname1().equals("-") || area.getAreaname1().equals("NA")) {
                    sidoName = area.getAreaname2();
                    weatherRootQuery = weatherDao.getDailyWeatherNodeByRegionAndDate(sidoName, sggName, umdName, RegionUnit.SIDO);
                } else {
                    sidoName = area.getAreaname1();
                    sggName = area.getAreaname2() + " " + area.getAreaname3();
                    sggName = sggName.replaceAll("-|NA", "");

                    if(area.getAreaname3().equals("-") || area.getAreaname3().equals("NA")) {
                        sggName = sggName.replaceAll(" ", "");
                    }

                    if (area.getAreaname4().equals("-") || area.getAreaname4().equals("NA")) {
                        weatherRootQuery = weatherDao.getDailyWeatherNodeByRegionAndDate(sidoName, sggName, umdName, RegionUnit.SIDO_SGG);
                    } else if(!area.getAreaname4().equals("-") && !area.getAreaname4().equals("NA")){
                        umdName = area.getAreaname4();
                        weatherRootQuery = weatherDao.getDailyWeatherNodeByRegionAndDate(sidoName, sggName, umdName, RegionUnit.SIDO_SGG_UMD);
                    }
                }
            }

            // TODO: tfh가 예보 시간인건가.. 예보시간이라면 hourlyWeather로 넣어야할 듯(?) 
            for(Days day : area.getDayList()) {
                List<DailyWeather> dailyWeatherList = weatherRootQuery.getDailyWeather();
                DailyWeather newDailyWeather = new DailyWeather();

                if (dailyWeatherList.size() != 0) {
                    DailyWeather oldDailyWeather = dailyWeatherList.get(0);
                    newDailyWeather.setDailyWeather(oldDailyWeather.getUid(), day.getTm(), day.getIcon(), day.getWtext(), day.getMintemp(), day.getMaxtemp(), day.getRainp());
                    weatherDao.updateWeatherNode(oldDailyWeather);
                } else {
                    newDailyWeather.setDailyWeather(null, day.getTm(), day.getIcon(), day.getWtext(), day.getMintemp(), day.getMaxtemp(), day.getRainp());

                    Regions region = weatherRootQuery.getRegion().get(0);
                    region.getDailyWeathers().add(newDailyWeather);
                    regionDao.updateRegionNode(region);

                    DailyWeather foundDailyWeather = weatherDao.getDailyWeatherNodeByDate(region.getUid(), day.getTm());

                    dayNode.getDailyWeathers().add(foundDailyWeather);
                    dateDao.updateDateNode(dayNode);

                    // 하루씩 증가?
                    dateMap.put(DateUnit.DAY, ++currentDay);
                    dayNode = dateDao.getDayNode(dateMap);
                }
                log.info("[Service] callKweatherDay7Api - " + dayNode.getDay() + "일의 " + "지역->날씨<-날짜 노드 연결 완료");
            }
            log.info("[Service] callKweatherDay7Api - 지역 : " + area.getAreaname1() + area.getAreaname2() + area.getAreaname3() + area.getAreaname4());
        }
    }

    private void connectRegionAndWeatherAndDateNode(Map<DateUnit, Integer> dateMap, String date, String time, List<Regions> regionByGrid, HourlyWeather hourlyWeather, ExternalWeatherApi api) {
        Hour hourNode = dateDao.getHourNode(dateMap);

        for(Regions oneRegion : regionByGrid) {
            WeatherRootQuery weatherRootQuery = weatherDao.getHourlyWeatherNodeByRegionAndDate(oneRegion.getUid());

            List<HourlyWeather> hourlyWeatherList = weatherRootQuery.getHourlyWeather();
            HourlyWeather newHourlyWeather = hourlyWeather;

            if(hourlyWeatherList.size() != 0) {
                HourlyWeather oldHourlyWeather = hourlyWeatherList.get(0);
                hourlyWeather.setUid(oldHourlyWeather.getUid());
                weatherDao.updateWeatherNode(oldHourlyWeather);
            } else {

                Regions region = weatherRootQuery.getRegion().get(0);
                region.getHourlyWeathers().add(newHourlyWeather);
                regionDao.updateRegionNode(region);

                HourlyWeather foundHourlyWeather = new HourlyWeather();

                if(api.equals(ExternalWeatherApi.FORECAST_GRIB)) {
                    foundHourlyWeather = weatherDao.getHourlyWeatherNodeByDate(region.getUid(), date, time, ExternalWeatherApi.FORECAST_GRIB);
                } else if(api.equals(ExternalWeatherApi.FORECAST_TIME)) {
                    foundHourlyWeather = weatherDao.getHourlyWeatherNodeByDate(region.getUid(), date, time, ExternalWeatherApi.FORECAST_TIME);
                } else if(api.equals(ExternalWeatherApi.FORECAST_SPACE)) {
                    foundHourlyWeather = weatherDao.getHourlyWeatherNodeByDate(region.getUid(), date, time, ExternalWeatherApi.FORECAST_SPACE);
                }

                hourNode.getHourlyWeathers().add(foundHourlyWeather);
            }
        }
        dateDao.updateDateNode(hourNode);

        log.info("[Service] connectRegionAndWeatherAndDateNode - " + hourNode.getHour() + "시의 " + "지역->날씨<-날짜 노드 연결 완료");
    }

    private int getFcstTimeCount(String baseTime) {
        int count = 0;
        Integer baseHour = Integer.parseInt(baseTime.substring(0, 2));

        if(baseHour % 3 == 0) {
            count = 3;
        } else if(baseHour % 3 == 1) {
            count = 2;
        } else if(baseHour % 3 == 2) {
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
