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
import com.example.realpilot.externalApiModel.kweatherAmPm7.AreaOfAmPm7;
import com.example.realpilot.externalApiModel.kweatherAmPm7.DayOfAmPm7;
import com.example.realpilot.externalApiModel.kweatherAmPm7.KweatherAmPm7TopModel;
import com.example.realpilot.externalApiModel.kweatherDay7.AreaOfDay7;
import com.example.realpilot.externalApiModel.kweatherDay7.DayOfDay7;
import com.example.realpilot.externalApiModel.kweatherDay7.KweatherDay7TopModel;
import com.example.realpilot.externalApiModel.kweatherShko.AreaOfShko;
import com.example.realpilot.externalApiModel.kweatherShko.KweatherShkoTopModel;
import com.example.realpilot.externalApiModel.weatherWarning.WeatherWarningTopModel;
import com.example.realpilot.externalApiModel.weatherWarning.WeatherWarning;
import com.example.realpilot.model.date.Day;
import com.example.realpilot.model.date.Hour;
import com.example.realpilot.model.region.Regions;
import com.example.realpilot.model.weather.*;
import com.example.realpilot.utilAndConfig.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class WeatherService<T> {
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

    @Value("${api.serviceKey}")
    private String serviceKey;
    @Value("${forecastGrib.api.url}")
    private String forecastGribApiUrl;
    @Value("${forecastTime.api.url}")
    private String forecastTimeApiUrl;
    @Value("${forecastSpace.api.url}")
    private String forecastSpaceApiUrl;
    @Value("${kweatherDay7.api.url}")
    private String kweatherDay7ApiUrl;
    @Value("${kweatherAmPm7.api.url}")
    private String kweatherAmPm7ApiUrl;
    @Value("${kweatherShko.api.url}")
    private String kweatherShkoApiUrl;
    @Value("${weatherWarning.api.url}")
    private String weatherWarningApiUrl;

    private Integer GRID_X_IDNEX = 0;
    private Integer GRID_Y_INDEX = 1;

    // 동네예보 API(초단기실황/초단기예보/동네예보)
    public void callWeatherApiByGrid() {
        Set<List<Integer>> gridSet = regionService.gridSet;

        ForecastGribTopModel forecastGribTopModel = new ForecastGribTopModel();
        ForecastTimeTopModel forecastTimeTopModel = new ForecastTimeTopModel();
        ForecastSpaceTopModel forecastSpaceTopModel = new ForecastSpaceTopModel();

        // TODO: 엑셀 파일에서 로드한 메모리상에 있는 grid 데이터를 사용하는게 아니라, DB에 있는 grid 데이터를 사용하는게 나을지?
       for(List<Integer> grid : gridSet) {
           Integer gridX = grid.get(GRID_X_IDNEX);
           Integer gridY = grid.get(GRID_Y_INDEX);

           List<Regions> regionByGrid = regionDao.getRegionNodeWithGrid(gridX, gridY);

           String baseDate = dateService.makeCurrentDateFormat();
           String baseTime = dateService.makeCurrentTimeFormat(ExternalWeatherApi.FORECAST_GRIB);
           // TODO: 고정시킨 시간 값 지우기
           baseTime = "0800";

           String parameters = "?ServiceKey=" + serviceKey + "&base_date=" + baseDate + "&base_time=" + baseTime + "&nx=" + gridX + "&ny=" + gridY + "&numOfRows=300&_type=json";
           URI forecastGribUri = URI.create(forecastGribApiUrl + parameters);
           URI forecastTimeUri = URI.create(forecastTimeApiUrl + parameters);
           URI forecastSpaceUri = URI.create(forecastSpaceApiUrl + parameters);


           callForecastGribApi(forecastGribTopModel, forecastGribUri, baseDate, baseTime, regionByGrid);
           //callForecastTimeApi(forecastTimeTopModel, forecastTimeUri, baseDate, baseTime, regionByGrid);

           baseTime = dateService.makeCurrentTimeFormat(ExternalWeatherApi.FORECAST_SPACE);
           //callForecastSpaceApi(forecastSpaceTopModel, forecastSpaceUri, baseDate, baseTime, regionByGrid);
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
        hourlyWeather.setHourlyWeather(null, categoryValueMap, baseDate, baseTime);

        Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();

        connectRegionAndHourlyWeatherAndDateNode(dateMap, baseDate, baseTime, regionByGrid, hourlyWeather, ExternalWeatherApi.FORECAST_GRIB);
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

            connectRegionAndHourlyWeatherAndDateNode(dateMap, fcstDate, fcstTime, regionByGrid, hourlyWeather, ExternalWeatherApi.FORECAST_TIME);
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

            connectRegionAndHourlyWeatherAndDateNode(dateMap, fcstDate, fcstTime, regionByGrid, hourlyWeather, ExternalWeatherApi.FORECAST_SPACE);
        }
    }

    private void connectRegionAndHourlyWeatherAndDateNode(Map<DateUnit, Integer> dateMap, String date, String time, List<Regions> regionByGrid, HourlyWeather hourlyWeather, ExternalWeatherApi api) {
        Optional<Hour> hourNode = dateDao.getHourNode(dateMap);

        for(Regions oneRegion : regionByGrid) {
            Regions foundRegion = new Regions();

            WeatherRootQuery weatherRootQuery = weatherDao.getAlreadyExistingWeatherNodeWithRegionUidAndDate(oneRegion.getUid());

            List<HourlyWeather> hourlyWeatherList = weatherRootQuery.getHourlyWeather();
            HourlyWeather newHourlyWeather = hourlyWeather;

            if(Optional.ofNullable(hourlyWeatherList).isPresent() && !hourlyWeatherList.isEmpty()) {
                HourlyWeather oldHourlyWeather = hourlyWeatherList.get(0);
                hourlyWeather.setUid(oldHourlyWeather.getUid());
                weatherDao.updateWeatherNode(hourlyWeather);
            } else {
                Optional.ofNullable(weatherRootQuery.getRegion()).ifPresent(region -> foundRegion.setUid(region.get(0).getUid()));

                foundRegion.getHourlyWeathers().add(newHourlyWeather);
                regionDao.updateRegionNode(foundRegion);

                Optional<HourlyWeather> foundHourlyWeather = Optional.empty();

                if(api.equals(ExternalWeatherApi.FORECAST_GRIB)) {
                    foundHourlyWeather = weatherDao.getHourlyWeatherNodeLinkedToRegionWithRegionUidAndDate(foundRegion.getUid(), date, time, ExternalWeatherApi.FORECAST_GRIB);
                } else if(api.equals(ExternalWeatherApi.FORECAST_TIME)) {
                    foundHourlyWeather = weatherDao.getHourlyWeatherNodeLinkedToRegionWithRegionUidAndDate(foundRegion.getUid(), date, time, ExternalWeatherApi.FORECAST_TIME);
                } else if(api.equals(ExternalWeatherApi.FORECAST_SPACE)) {
                    foundHourlyWeather = weatherDao.getHourlyWeatherNodeLinkedToRegionWithRegionUidAndDate(foundRegion.getUid(), date, time, ExternalWeatherApi.FORECAST_SPACE);
                }

                if(Optional.ofNullable(hourNode).isPresent()) {
                    Optional<HourlyWeather> finalFoundHourlyWeather = foundHourlyWeather;
                    hourNode.ifPresent(hour -> hour.getHourlyWeathers().add(finalFoundHourlyWeather.get()));
                }
            }
        }
        dateDao.updateDateNode(hourNode);

        hourNode.ifPresent(hour -> log.info("[Service] connectRegionAndWeatherAndDateNode - " + hour.getHour() + "시의 " + "지역->날씨<-날짜 노드 연결 완료"));
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

    public void callWeatherApiOfKweather() {
        KweatherDay7TopModel kweatherDay7TopModel = new KweatherDay7TopModel();
        KweatherAmPm7TopModel kweatherAmPm7TopModel = new KweatherAmPm7TopModel();
        KweatherShkoTopModel kweatherShkoTopModel = new KweatherShkoTopModel();

        URI kweatherDay7Uri = URI.create(kweatherDay7ApiUrl);
        URI kweatherAmPm7Uri = URI.create(kweatherAmPm7ApiUrl);
        URI kweatherShkoUri = URI.create(kweatherShkoApiUrl);

        //callKweatherDay7Api(kweatherDay7TopModel, kweatherDay7Uri);
        //callKweatherAmPm7Api(kweatherAmPm7TopModel, kweatherAmPm7Uri);
        callKweatherShkoApi(kweatherShkoTopModel, kweatherShkoUri);
    }

    private void callKweatherDay7Api(KweatherDay7TopModel kweatherDay7TopModel, URI kweatherDay7Uri) {
        try {
            kweatherDay7TopModel = restTemplate.getForObject(kweatherDay7Uri, KweatherDay7TopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // TODO: TopModel도 Optional 사용
        for (AreaOfDay7 area : kweatherDay7TopModel.getAreas()) {
            Regions foundRegion = new Regions();

            String sidoName = "";
            String sggName = "";
            String umdName = "";

            WeatherRootQuery weatherRootQuery = null;

            if(area.getAreaname1().equals(ConnectedSidoUmd.SEJONG.getSidoName())) {
                sidoName = area.getAreaname1();
                umdName = area.getAreaname2();
                checkAlreadyExistingWeatherNode(weatherRootQuery, foundRegion, sidoName, sggName, umdName, RegionUnit.SIDO_UMD, DateUnit.DAY, Query.DAILY_WEATHER);
            } else {
                if (area.getAreaname1().equals("-") || area.getAreaname1().equals("NA")) {
                    sidoName = area.getAreaname2();
                    checkAlreadyExistingWeatherNode(weatherRootQuery, foundRegion, sidoName, sggName, umdName, RegionUnit.SIDO, DateUnit.DAY, Query.DAILY_WEATHER);
                } else if(area.getAreaname2().equals("NA")) {
                    sidoName = area.getAreaname1();
                    checkAlreadyExistingWeatherNode(weatherRootQuery, foundRegion, sidoName, sggName, umdName, RegionUnit.SIDO, DateUnit.DAY, Query.DAILY_WEATHER);
                } else {
                    sidoName = area.getAreaname1();
                    sggName = area.getAreaname2() + " " + area.getAreaname3();
                    sggName = sggName.replaceAll("-|NA", "");

                    if(area.getAreaname3().equals("-") || area.getAreaname3().equals("NA")) {
                        sggName = sggName.replaceAll(" ", "");
                    }

                    if (area.getAreaname4().equals("-") || area.getAreaname4().equals("NA")) {
                        checkAlreadyExistingWeatherNode(weatherRootQuery, foundRegion, sidoName, sggName, umdName, RegionUnit.SIDO_SGG, DateUnit.DAY, Query.DAILY_WEATHER);
                    } else if(!area.getAreaname4().equals("-") && !area.getAreaname4().equals("NA")){
                        umdName = area.getAreaname4();
                        checkAlreadyExistingWeatherNode(weatherRootQuery, foundRegion, sidoName, sggName, umdName, RegionUnit.SIDO_SGG_UMD, DateUnit.DAY, Query.DAILY_WEATHER);
                    }
                }
            }

            List<DailyWeather> dailyWeatherList = weatherRootQuery.getDailyWeather();

            if(Optional.ofNullable(foundRegion.getUid()).isPresent()) {
                DailyWeather newDailyWeather = new DailyWeather();

                if (Optional.ofNullable(dailyWeatherList).isPresent() && !dailyWeatherList.isEmpty()) {
                    for (DayOfDay7 day : area.getDayList()) {
                        DailyWeather oldDailyWeather = dailyWeatherList.get(0);
                        newDailyWeather.setDailyWeather(oldDailyWeather.getUid(), day);
                        weatherDao.updateWeatherNode(newDailyWeather);
                    }
                } else {
                    for (DayOfDay7 oneDay : area.getDayList()) {
                        newDailyWeather = new DailyWeather();
                        newDailyWeather.setDailyWeather(null, oneDay);

                        foundRegion.getDailyWeathers().add(newDailyWeather);
                    }

                    regionDao.updateRegionNode(foundRegion);

                    for (DayOfDay7 oneDay : area.getDayList()) {
                        AtomicReference<Optional<DailyWeather>> foundDailyWeather = new AtomicReference<>();
                        Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundDailyWeather.set(weatherDao.getDailyWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, oneDay.getTm())));

                        Map<DateUnit, Integer> dateMap = dateService.getTmDate(oneDay.getTm());
                        Optional<Day> dayNode = dateDao.getDayNode(dateMap);

                        dayNode.ifPresent(day -> day.getDailyWeathers().add(foundDailyWeather.get().get()));
                        dateDao.updateDateNode(dayNode);
                        dayNode.ifPresent(day -> log.info("[Service] callKweatherDay7Api - " + day.getDay() + "일의 " + "지역->날씨<-날짜 노드 연결 완료"));
                    }
                }
            }
            log.info("[Service] callKweatherDay7Api - 지역 : " + "[" + area.getCode() + "] " + area.getAreaname1() + area.getAreaname2() + area.getAreaname3() + area.getAreaname4());
        }
    }

    private void callKweatherAmPm7Api(KweatherAmPm7TopModel kweatherAmPm7TopModel, URI kweatherAmPm7Uri) {
        try {
            kweatherAmPm7TopModel = restTemplate.getForObject(kweatherAmPm7Uri, KweatherAmPm7TopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (AreaOfAmPm7 area : kweatherAmPm7TopModel.getAreas()) {
            Regions foundRegion = new Regions();

            String sidoName = "";
            String sggName = "";
            String umdName = "";

            WeatherRootQuery weatherRootQueryForAm = null;
            WeatherRootQuery weatherRootQueryForPm = null;

            Optional<RegionUnit> regionUnit = Optional.empty();

            if(area.getAreaname1().equals(ConnectedSidoUmd.SEJONG.getSidoName())) {
                sidoName = area.getAreaname1();
                umdName = area.getAreaname2();
                checkAlreadyExistingWeatherNode(weatherRootQueryForAm, foundRegion, sidoName, sggName, umdName, RegionUnit.SIDO_UMD, DateUnit.DAY, Query.AM_WEATHER);
                weatherRootQueryForPm = weatherDao.getAlreadyExistingWeatherNodeWithRegionNameAndDate(sidoName, sggName, umdName, RegionUnit.SIDO_UMD, DateUnit.DAY, Query.PM_WEATHER);
                regionUnit = Optional.of(RegionUnit.SIDO_UMD);
            } else {
                if (area.getAreaname1().equals("-") || area.getAreaname1().equals("NA")) {
                    sidoName = area.getAreaname2();
                    checkAlreadyExistingWeatherNode(weatherRootQueryForAm, foundRegion, sidoName, sggName, umdName, RegionUnit.SIDO, DateUnit.DAY, Query.AM_WEATHER);
                    weatherRootQueryForPm = weatherDao.getAlreadyExistingWeatherNodeWithRegionNameAndDate(sidoName, sggName, umdName, RegionUnit.SIDO, DateUnit.DAY, Query.PM_WEATHER);
                    regionUnit = Optional.of(RegionUnit.SIDO);
                } else if(area.getAreaname2().equals("NA")) {
                    sidoName = area.getAreaname1();
                    checkAlreadyExistingWeatherNode(weatherRootQueryForAm, foundRegion, sidoName, sggName, umdName, RegionUnit.SIDO, DateUnit.DAY, Query.AM_WEATHER);
                    weatherRootQueryForPm = weatherDao.getAlreadyExistingWeatherNodeWithRegionNameAndDate(sidoName, sggName, umdName, RegionUnit.SIDO, DateUnit.DAY, Query.PM_WEATHER);
                    regionUnit = Optional.of(RegionUnit.SIDO);

                } else {
                    sidoName = area.getAreaname1();
                    sggName = area.getAreaname2() + " " + area.getAreaname3();
                    sggName = sggName.replaceAll("-|NA", "");

                    if(area.getAreaname3().equals("-") || area.getAreaname3().equals("NA")) {
                        sggName = sggName.replaceAll(" ", "");
                    }

                    if (area.getAreaname4().equals("-") || area.getAreaname4().equals("NA")) {
                        checkAlreadyExistingWeatherNode(weatherRootQueryForAm, foundRegion, sidoName, sggName, umdName, RegionUnit.SIDO_SGG, DateUnit.DAY, Query.AM_WEATHER);
                        weatherRootQueryForPm = weatherDao.getAlreadyExistingWeatherNodeWithRegionNameAndDate(sidoName, sggName, umdName, RegionUnit.SIDO_SGG, DateUnit.DAY, Query.PM_WEATHER);
                        regionUnit = Optional.of(RegionUnit.SIDO_SGG);
                    } else if(!area.getAreaname4().equals("-") && !area.getAreaname4().equals("NA")){
                        umdName = area.getAreaname4();
                        checkAlreadyExistingWeatherNode(weatherRootQueryForAm, foundRegion, sidoName, sggName, umdName, RegionUnit.SIDO_SGG_UMD, DateUnit.DAY, Query.AM_WEATHER);
                        weatherRootQueryForPm = weatherDao.getAlreadyExistingWeatherNodeWithRegionNameAndDate(sidoName, sggName, umdName, RegionUnit.SIDO_SGG_UMD, DateUnit.DAY, Query.PM_WEATHER);
                        regionUnit = Optional.of(RegionUnit.SIDO_SGG_UMD);
                    }
                }
            }

            AtomicReference<List<AmWeather>> amWeatherList = new AtomicReference<>();
            Optional.ofNullable(weatherRootQueryForAm.getAmWeather()).ifPresent(amWeather -> amWeatherList.set(amWeather));
            AtomicReference<List<PmWeather>> pmWeatherList = new AtomicReference<>();
            Optional.ofNullable(weatherRootQueryForPm.getPmWeather()).ifPresent(pmWeather -> pmWeatherList.set(pmWeather));

            if(Optional.ofNullable(foundRegion.getUid()).isPresent()) {
                Regions prevFoundRegion = new Regions();
                Optional.ofNullable(regionUnit).ifPresent(foundRegionUnit -> prevFoundRegion.setRegionUidAndName(foundRegion, foundRegionUnit.get()));
                connectRegionAndAmWeatherAndDateNode(area, foundRegion, amWeatherList);
                connectRegionAndPmWeatherAndDateNode(area, prevFoundRegion, pmWeatherList);
            }
            log.info("[Service] callKweatherAmPm7Api - 지역 : " + "[" + area.getCode() + "] " + area.getAreaname1() + area.getAreaname2() + area.getAreaname3() + area.getAreaname4());
        }
    }

    private void connectRegionAndAmWeatherAndDateNode(AreaOfAmPm7 area, Regions foundRegion, AtomicReference<List<AmWeather>> amWeatherList) {
        AmWeather newAmWeather = new AmWeather();

        if (Optional.ofNullable(amWeatherList).isPresent() && !amWeatherList.get().isEmpty()) {
            for (DayOfAmPm7 day : area.getDayList()) {
                AmWeather oldAmWeather = amWeatherList.get().get(0);
                newAmWeather.setAmWeather(oldAmWeather.getUid(), day);
                weatherDao.updateWeatherNode(newAmWeather);
            }
        } else {
            for (DayOfAmPm7 oneDay : area.getDayList()) {
                newAmWeather = new AmWeather();
                newAmWeather.setAmWeather(null, oneDay);

                foundRegion.getAmWeathers().add(newAmWeather);
            }

            regionDao.updateRegionNode(foundRegion);

            for (DayOfAmPm7 oneDay : area.getDayList()) {
                AtomicReference<Optional<AmWeather>> foundAmWeather = new AtomicReference<>();
                Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundAmWeather.set(weatherDao.getAmWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, oneDay.getTm())));

                Map<DateUnit, Integer> dateMap = dateService.getTmDate(oneDay.getTm());
                Optional<Day> dayNode = dateDao.getDayNode(dateMap);

                dayNode.ifPresent(day -> day.getAmWeathers().add(foundAmWeather.get().get()));
                dateDao.updateDateNode(dayNode);
                dayNode.ifPresent(day -> log.info("[Service] callKweatherAmPm7Api - " + day.getDay() + "일의 " + "지역->날씨<-날짜 노드 연결 완료"));
            }
        }
    }

    private void connectRegionAndPmWeatherAndDateNode(AreaOfAmPm7 area, Regions foundRegion, AtomicReference<List<PmWeather>> pmWeatherList) {
        PmWeather newPmWeather = new PmWeather();

        if (Optional.ofNullable(pmWeatherList).isPresent() && !pmWeatherList.get().isEmpty()) {
            for (DayOfAmPm7 day : area.getDayList()) {
                PmWeather oldPmWeather = pmWeatherList.get().get(0);
                newPmWeather.setPmWeather(oldPmWeather.getUid(), day);
                weatherDao.updateWeatherNode(newPmWeather);
            }
        } else {
            for (DayOfAmPm7 oneDay : area.getDayList()) {
                newPmWeather = new PmWeather();
                newPmWeather.setPmWeather(null, oneDay);

                foundRegion.getPmWeathers().add(newPmWeather);
            }

            regionDao.updateRegionNode(foundRegion);

            for (DayOfAmPm7 oneDay : area.getDayList()) {
                AtomicReference<Optional<PmWeather>> foundPmWeather = new AtomicReference<>();
                Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundPmWeather.set(weatherDao.getPmWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, oneDay.getTm())));

                Map<DateUnit, Integer> dateMap = dateService.getTmDate(oneDay.getTm());
                Optional<Day> dayNode = dateDao.getDayNode(dateMap);

                dayNode.ifPresent(day -> day.getPmWeathers().add(foundPmWeather.get().get()));
                dateDao.updateDateNode(dayNode);
                dayNode.ifPresent(day -> log.info("[Service] callKweatherAmPm7Api - " + day.getDay() + "일의 " + "지역->날씨<-날짜 노드 연결 완료"));
            }
        }
    }

    private void callKweatherShkoApi(KweatherShkoTopModel kweatherShkoTopModel, URI kweatherShkoUri) {
        try {
            kweatherShkoTopModel = restTemplate.getForObject(kweatherShkoUri, KweatherShkoTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (AreaOfShko area : kweatherShkoTopModel.getAreas()) {
            Regions foundRegion = new Regions();

            String sidoName = "";
            String sggName = "";
            String umdName = "";

            WeatherRootQuery weatherRootQuery = new WeatherRootQuery();

            if(area.getAreaname1().equals(ConnectedSidoUmd.SEJONG.getSidoName())) {
                sidoName = area.getAreaname1();
                umdName = area.getAreaname2();
                checkAlreadyExistingWeatherNode(weatherRootQuery, foundRegion, sidoName, sggName, umdName, RegionUnit.SIDO_UMD, DateUnit.HOUR, Query.HOURLY_WEATHER);
            } else {
                if (area.getAreaname1().equals("-") || area.getAreaname1().equals("NA")) {
                    sidoName = area.getAreaname2();
                    checkAlreadyExistingWeatherNode(weatherRootQuery, foundRegion, sidoName, sggName, umdName, RegionUnit.SIDO, DateUnit.HOUR, Query.HOURLY_WEATHER);
                } else if(area.getAreaname2().equals("NA")) {
                    sidoName = area.getAreaname1();
                    checkAlreadyExistingWeatherNode(weatherRootQuery, foundRegion, sidoName, sggName, umdName, RegionUnit.SIDO, DateUnit.HOUR, Query.HOURLY_WEATHER);
                } else {
                    sidoName = area.getAreaname1();
                    sggName = area.getAreaname2() + " " + area.getAreaname3();
                    sggName = sggName.replaceAll("-|NA", "");

                    if(area.getAreaname3().equals("-") || area.getAreaname3().equals("NA")) {
                        sggName = sggName.replaceAll(" ", "");
                    }

                    if (area.getAreaname4().equals("-") || area.getAreaname4().equals("NA")) {
                        checkAlreadyExistingWeatherNode(weatherRootQuery, foundRegion, sidoName, sggName, umdName, RegionUnit.SIDO_SGG, DateUnit.HOUR, Query.HOURLY_WEATHER);
                    } else if(!area.getAreaname4().equals("-") && !area.getAreaname4().equals("NA")){
                        umdName = area.getAreaname4();
                        checkAlreadyExistingWeatherNode(weatherRootQuery, foundRegion, sidoName, sggName, umdName, RegionUnit.SIDO_SGG_UMD, DateUnit.HOUR, Query.HOURLY_WEATHER);
                    }
                }
            }

            List<HourlyWeather> hourlyWeatherList = weatherRootQuery.getHourlyWeather();

            if(Optional.ofNullable(foundRegion.getUid()).isPresent()) {
                HourlyWeather newHourlyWeather = new HourlyWeather();

                if (Optional.ofNullable(hourlyWeatherList).isPresent() && !hourlyWeatherList.isEmpty()) {
                    HourlyWeather oldHourlyWeather = hourlyWeatherList.get(0);
                    newHourlyWeather.setHourlyWeather(oldHourlyWeather.getUid(), area);
                    weatherDao.updateWeatherNode(newHourlyWeather);
                } else {
                    newHourlyWeather = new HourlyWeather();
                    newHourlyWeather.setHourlyWeather(null, area);

                    foundRegion.getHourlyWeathers().add(newHourlyWeather);

                    regionDao.updateRegionNode(foundRegion);

                    Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();
                    String time = dateMap.get(DateUnit.HOUR).toString() + "00";

                    AtomicReference<Optional<HourlyWeather>> foundHourlyWeather = new AtomicReference<>();
                    Optional.ofNullable(foundRegion.getUid())
                            .ifPresent(uid -> foundHourlyWeather.set(weatherDao.getHourlyWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, area.getTm(), time, ExternalWeatherApi.KWEATHER_SHKO)));

                    Optional<Hour> hourNode = dateDao.getHourNode(dateMap);

                    hourNode.ifPresent(hour -> hour.getHourlyWeathers().add(foundHourlyWeather.get().get()));
                    dateDao.updateDateNode(hourNode);
                }
            }
            log.info("[Service] callKweatherShkoApi - [" + area.getCode() + "] " + area.getAreaname1() + area.getAreaname2() + area.getAreaname3() + area.getAreaname4() + " 지역->날씨<-날짜 노드 연결 완료");
        }

    }

    private void checkAlreadyExistingWeatherNode(WeatherRootQuery weatherRootQuery, Regions foundRegion, String sidoName, String sggName, String umdName, RegionUnit regionUnit, DateUnit dateUnit, Query query) {
        weatherRootQuery.setWeatherRootQuery(weatherDao.getAlreadyExistingWeatherNodeWithRegionNameAndDate(sidoName, sggName, umdName, regionUnit, dateUnit, query));
        Optional.ofNullable(weatherRootQuery.getRegion()).ifPresent(region -> foundRegion.setRegionUidAndName(region.get(0), regionUnit));
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
