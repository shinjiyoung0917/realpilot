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
import com.example.realpilot.externalApiModel.specialWeather.*;
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
    @Value("${specialWeatherReport.api.url}")
    private String specialWeatherReportApiUrl;

    private Integer GRID_X_IDNEX = 0;
    private Integer GRID_Y_INDEX = 1;

    // 동네예보 API(초단기실황/초단기예보/동네예보)
    public void callWeatherApiOfKma() {
        Set<List<Integer>> gridSet = regionService.gridSet;

        ForecastGribTopModel forecastGribTopModel = new ForecastGribTopModel();
        ForecastTimeTopModel forecastTimeTopModel = new ForecastTimeTopModel();
        ForecastSpaceTopModel forecastSpaceTopModel = new ForecastSpaceTopModel();

        Optional<Set<Regions>> optionalGridList = regionDao.getGridList();

       for(Regions region : optionalGridList.get()) {
           Integer gridX = region.getGridX();
           Integer gridY = region.getGridY();

           List<Regions> regionList = regionDao.getRegionNodeWithGrid(gridX, gridY);

           String releaseDate = dateService.makeCurrentDateFormat();
           String releaseTime = dateService.makeCurrentTimeFormat(ExternalWeatherApi.FORECAST_GRIB);
           releaseTime = "1800";

           String parameters = "?ServiceKey=" + serviceKey + "&base_date=" + releaseDate + "&base_time=" + releaseTime + "&nx=" + gridX + "&ny=" + gridY + "&numOfRows=300&_type=json";
           URI forecastGribUri = URI.create(forecastGribApiUrl + parameters);
           URI forecastTimeUri = URI.create(forecastTimeApiUrl + parameters);
           URI forecastSpaceUri = URI.create(forecastSpaceApiUrl + parameters);


           callForecastGribApi(forecastGribTopModel, forecastGribUri, releaseDate, releaseTime, regionList);
           callForecastTimeApi(forecastTimeTopModel, forecastTimeUri, releaseDate, releaseTime, regionList);

           releaseTime = dateService.makeCurrentTimeFormat(ExternalWeatherApi.FORECAST_SPACE);
           callForecastSpaceApi(forecastSpaceTopModel, forecastSpaceUri, releaseDate, releaseTime, regionList);
       }
    }

    private void callForecastGribApi(ForecastGribTopModel forecastGribTopModel,URI forecastGribUri, String releaseDate, String releaseTime, List<Regions> regionList) {
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
        hourlyWeather.setHourlyWeather(null, categoryValueMap, releaseDate, releaseTime);

        Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();

        connectRegionAndHourlyWeatherAndDateNode(dateMap, releaseDate, releaseTime, regionList, hourlyWeather, ExternalWeatherApi.FORECAST_GRIB);
    }

    private void callForecastTimeApi(ForecastTimeTopModel forecastTimeTopModel, URI forecastTimeUri, String releaseDate, String releaseTime, List<Regions> regionList) {
        try {
            forecastTimeTopModel = restTemplate.getForObject(forecastTimeUri, ForecastTimeTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int forecastTimeCount = getForecastTimeCount(releaseDate); // 예보시간의 구간 수 (2개 or 3개 or 4개)
        int index = 0;

        Map<String, Float>[] categoryValueMapArray = new Map[4];
        for(int i=0 ; i < categoryValueMapArray.length ; ++i) {
            categoryValueMapArray[i] = new HashMap<>();
        }

        String forecastDate = "";
        String[] forecasTimeArray = new String[4];

        // TODO: 반복문 돌면서 ~getItem 계속 호출되니 미리 정의해두고 사용하기
        for (ForecastTime forecastTime : forecastTimeTopModel.getResponse().getBody().getItems().getItem()) {
            log.info("[Service] callForecastTimeApi - forecastTime : " + forecastTime.getFcstTime());
            log.info("[Service] callForecastTimeApi - category : " + forecastTime.getCategory());
            log.info("[Service] callForecastTimeApi - value : " + forecastTime.getFcstValue());

            // TODO: index 변수 대신에 현재 시간과 예보 시간의 차이(예보시간-현재시간-1)를 배열의 인덱스로 두어 저장하도록 수정
            forecastDate = forecastTime.getFcstDate();
            if(index % forecastTimeCount ==  0) {
                categoryValueMapArray[0].put(forecastTime.getCategory(), forecastTime.getFcstValue());
                forecasTimeArray[0] = forecastTime.getFcstTime();
            } else if(index % forecastTimeCount ==  1) {
                categoryValueMapArray[1].put(forecastTime.getCategory(), forecastTime.getFcstValue());
                forecasTimeArray[1] = forecastTime.getFcstTime();
            } else if(index % forecastTimeCount ==  2) {
                categoryValueMapArray[2].put(forecastTime.getCategory(), forecastTime.getFcstValue());
                forecasTimeArray[2] = forecastTime.getFcstTime();
            } else if(index % forecastTimeCount ==  3) {
                categoryValueMapArray[3].put(forecastTime.getCategory(), forecastTime.getFcstValue());
                forecasTimeArray[3] = forecastTime.getFcstTime();
            }
            ++index;
        }

        List<HourlyWeather> hourlyWeatherList = new ArrayList<>();

        // TODO: index 변수 대신에 현재 시간과 예보 시간의 차이(예보시간-현재시간-1)를 배열의 인덱스로 두어 저장하도록 수정
        for(int i=0 ; i < forecastTimeCount ; ++i) {
            HourlyWeather hourlyWeather = new HourlyWeather();
            if(i == 0) {
                hourlyWeather.setHourlyWeather(null, categoryValueMapArray[0], releaseDate, releaseTime, forecastDate, forecasTimeArray[0]);
            } else  if(i == 1) {
                hourlyWeather.setHourlyWeather(null, categoryValueMapArray[1], releaseDate, releaseTime, forecastDate, forecasTimeArray[1]);
            } else  if(i == 2) {
                hourlyWeather.setHourlyWeather(null, categoryValueMapArray[2], releaseDate, releaseTime, forecastDate, forecasTimeArray[2]);
            } else  if(i == 3) {
                hourlyWeather.setHourlyWeather(null, categoryValueMapArray[3], releaseDate, releaseTime, forecastDate, forecasTimeArray[3]);
            }
            hourlyWeatherList.add(hourlyWeather);
        }

        for(HourlyWeather hourlyWeather : hourlyWeatherList) {
            String forecastTime = hourlyWeather.getForecastTime();
            Map<DateUnit, Integer> dateMap = dateService.splitDateAndTime(forecastDate, forecastTime);

            connectRegionAndHourlyWeatherAndDateNode(dateMap, forecastDate, forecastTime, regionList, hourlyWeather, ExternalWeatherApi.FORECAST_TIME);
        }
    }

    private void callForecastSpaceApi(ForecastSpaceTopModel forecastSpaceTopModel, URI forecastSpaceUri, String releaseDate, String releaseTime, List<Regions> regionList) {
        try {
            forecastSpaceTopModel = restTemplate.getForObject(forecastSpaceUri, ForecastSpaceTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int index = 0;

        String prevForecastDate = "";
        String prevForecastTime = "";

        Map<String, Float> categoryValueMap = new HashMap<>();
        List<Map<String, Float>> categoryValueMapList = new ArrayList<>();
        List<HourlyWeather> hourlyWeatherList = new ArrayList<>();

        for(ForecastSpace forecastSpace : forecastSpaceTopModel.getResponse().getBody().getItems().getItem()) {
            HourlyWeather hourlyWeather = new HourlyWeather();

            if(!prevForecastTime.equals(forecastSpace.getFcstTime()) && index != 0) {
                hourlyWeather.setHourlyWeather(null, categoryValueMap, releaseDate, releaseTime, prevForecastDate, prevForecastTime);
                hourlyWeatherList.add(hourlyWeather);
                categoryValueMap = new HashMap<>();
            } else {
                categoryValueMap.put(forecastSpace.getCategory(), forecastSpace.getFcstValue());
                categoryValueMapList.add(categoryValueMap);
            }
            prevForecastDate = forecastSpace.getFcstDate();
            prevForecastTime = forecastSpace.getFcstTime();

            ++index;
        }

        for(HourlyWeather hourlyWeather : hourlyWeatherList) {
            String forecastDate = hourlyWeather.getForecastDate();
            String forecastTime = hourlyWeather.getForecastTime();

            Map<DateUnit, Integer> dateMap = dateService.splitDateAndTime(forecastDate, forecastTime);

            connectRegionAndHourlyWeatherAndDateNode(dateMap, forecastDate, forecastTime, regionList, hourlyWeather, ExternalWeatherApi.FORECAST_SPACE);
        }
    }

    private void connectRegionAndHourlyWeatherAndDateNode(Map<DateUnit, Integer> dateMap, String date, String time, List<Regions> regionList, HourlyWeather hourlyWeather, ExternalWeatherApi api) {
        Optional<Hour> hourNode = dateDao.getHourNode(dateMap);

        if (Optional.ofNullable(regionList).isPresent() && !regionList.isEmpty()) {
            for (Regions oneRegion : regionList) {
                Regions foundRegion = new Regions();

                WeatherRootQuery weatherRootQuery = weatherDao.getAlreadyExistingWeatherNodeWithRegionUidAndDate(oneRegion.getUid(), dateMap, Query.HOURLY_WEATHER);

                List<HourlyWeather> hourlyWeatherList = weatherRootQuery.getHourlyWeather();
                HourlyWeather newHourlyWeather = hourlyWeather;

                if (Optional.ofNullable(hourlyWeatherList).isPresent() && !hourlyWeatherList.isEmpty()) {
                    HourlyWeather oldHourlyWeather = hourlyWeatherList.get(0);
                    hourlyWeather.setUid(oldHourlyWeather.getUid());
                    weatherDao.updateWeatherNode(hourlyWeather);
                } else {
                    foundRegion.setUid(oneRegion.getUid());

                    Optional<HourlyWeather> foundHourlyWeather1 = Optional.empty();

                    if (api.equals(ExternalWeatherApi.FORECAST_GRIB)) {
                        foundHourlyWeather1 = weatherDao.getHourlyWeatherNodeLinkedToRegionWithRegionUidAndDate(foundRegion.getUid(), date, time, Query.HOURLY_WEATHER);
                    } else if (api.equals(ExternalWeatherApi.FORECAST_TIME)) {
                        foundHourlyWeather1 = weatherDao.getHourlyWeatherNodeLinkedToRegionWithRegionUidAndDate(foundRegion.getUid(), date, time, Query.HOURLY_WEATHER);
                    } else if (api.equals(ExternalWeatherApi.FORECAST_SPACE)) {
                        foundHourlyWeather1 = weatherDao.getHourlyWeatherNodeLinkedToRegionWithRegionUidAndDate(foundRegion.getUid(), date, time, Query.HOURLY_WEATHER);
                    }

                    AtomicReference<String> hourlyWeatherUid = new AtomicReference<>();
                    foundHourlyWeather1.ifPresent(notNullHourlyWeather -> hourlyWeatherUid.set(notNullHourlyWeather.getUid()));

                    hourlyWeather.setUid(hourlyWeatherUid.get());

                    foundRegion.getHourlyWeathers().add(newHourlyWeather);
                    regionDao.updateRegionNode(foundRegion);

                    Optional<HourlyWeather> foundHourlyWeather2 = Optional.empty();

                    if (api.equals(ExternalWeatherApi.FORECAST_GRIB)) {
                        foundHourlyWeather2 = weatherDao.getHourlyWeatherNodeLinkedToRegionWithRegionUidAndDate(foundRegion.getUid(), date, time, Query.HOURLY_WEATHER);
                    } else if (api.equals(ExternalWeatherApi.FORECAST_TIME)) {
                        foundHourlyWeather2 = weatherDao.getHourlyWeatherNodeLinkedToRegionWithRegionUidAndDate(foundRegion.getUid(), date, time, Query.HOURLY_WEATHER);
                    } else if (api.equals(ExternalWeatherApi.FORECAST_SPACE)) {
                        foundHourlyWeather2 = weatherDao.getHourlyWeatherNodeLinkedToRegionWithRegionUidAndDate(foundRegion.getUid(), date, time, Query.HOURLY_WEATHER);
                    }

                    if (Optional.ofNullable(foundHourlyWeather2).isPresent()) {
                        Optional<HourlyWeather> finalFoundHourlyWeather = foundHourlyWeather2;
                        hourNode.ifPresent(hour -> hour.getHourlyWeathers().add(finalFoundHourlyWeather.get()));
                    }
                }
            }
        }
        dateDao.updateDateNode(hourNode);

        hourNode.ifPresent(hour -> log.info("[Service] connectRegionAndHourlyWeatherAndDateNode - " + hour.getHour() + "시의 " + "지역->날씨<-날짜 노드 연결 완료"));
    }

    private int getForecastTimeCount(String releaseDate) {
        int count = 0;
        Integer baseHour = Integer.parseInt(releaseDate.substring(0, 2));

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

        callKweatherDay7Api(kweatherDay7TopModel, kweatherDay7Uri);
        callKweatherAmPm7Api(kweatherAmPm7TopModel, kweatherAmPm7Uri);
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

            WeatherRootQuery weatherRootQuery = new WeatherRootQuery();

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
                        AtomicReference<Optional<DailyWeather>> foundDailyWeather = new AtomicReference<>(Optional.empty());
                        Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundDailyWeather.set(weatherDao.getDailyWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, oneDay.getTm())));

                        AtomicReference<String> dailyWeatherUid = new AtomicReference<>();
                        foundDailyWeather.get().ifPresent(notNullDailyWeather-> dailyWeatherUid.set(notNullDailyWeather.getUid()));

                        newDailyWeather = new DailyWeather();
                        newDailyWeather.setDailyWeather(dailyWeatherUid.get(), oneDay);

                        foundRegion.getDailyWeathers().add(newDailyWeather);
                    }

                    regionDao.updateRegionNode(foundRegion);

                    for (DayOfDay7 oneDay : area.getDayList()) {
                        AtomicReference<Optional<DailyWeather>> foundDailyWeather = new AtomicReference<>(Optional.empty());
                        Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundDailyWeather.set(weatherDao.getDailyWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, oneDay.getTm())));

                        Map<DateUnit, Integer> dateMap = dateService.splitDateIncludingDelim(oneDay.getTm());
                        Optional<Day> dayNode = dateDao.getDayNode(dateMap);

                        if(foundDailyWeather.get().isPresent()) {
                            dayNode.ifPresent(day -> day.getDailyWeathers().add(foundDailyWeather.get().get()));
                            dateDao.updateDateNode(dayNode);
                        }
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

            WeatherRootQuery weatherRootQueryForAm = new WeatherRootQuery();
            WeatherRootQuery weatherRootQueryForPm = new WeatherRootQuery();

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

        if (Optional.ofNullable(amWeatherList.get()).isPresent() && !amWeatherList.get().isEmpty()) {
            for (DayOfAmPm7 day : area.getDayList()) {
                AmWeather oldAmWeather = amWeatherList.get().get(0);
                newAmWeather.setAmWeather(oldAmWeather.getUid(), day);
                weatherDao.updateWeatherNode(newAmWeather);
            }
        } else {
            for (DayOfAmPm7 oneDay : area.getDayList()) {
                AtomicReference<Optional<AmWeather>> foundAmWeather = new AtomicReference<>(Optional.empty());
                Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundAmWeather.set(weatherDao.getAmWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, oneDay.getTm())));

                AtomicReference<String> amWeatherUid = new AtomicReference<>();
                foundAmWeather.get().ifPresent(notNullAmWeather-> amWeatherUid.set(notNullAmWeather.getUid()));

                newAmWeather = new AmWeather();
                newAmWeather.setAmWeather(amWeatherUid.get(), oneDay);

                foundRegion.getAmWeathers().add(newAmWeather);
            }

            regionDao.updateRegionNode(foundRegion);

            for (DayOfAmPm7 oneDay : area.getDayList()) {
                AtomicReference<Optional<AmWeather>> foundAmWeather = new AtomicReference<>(Optional.empty());
                Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundAmWeather.set(weatherDao.getAmWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, oneDay.getTm())));

                Map<DateUnit, Integer> dateMap = dateService.splitDateIncludingDelim(oneDay.getTm());
                Optional<Day> dayNode = dateDao.getDayNode(dateMap);

                if(foundAmWeather.get().isPresent()) {
                    dayNode.ifPresent(day -> day.getAmWeathers().add(foundAmWeather.get().get()));
                    dateDao.updateDateNode(dayNode);
                }
                dayNode.ifPresent(day -> log.info("[Service] callKweatherAmPm7Api - " + day.getDay() + "일의 " + "지역->날씨<-날짜 노드 연결 완료"));
            }
        }
    }

    private void connectRegionAndPmWeatherAndDateNode(AreaOfAmPm7 area, Regions foundRegion, AtomicReference<List<PmWeather>> pmWeatherList) {
        PmWeather newPmWeather = new PmWeather();

        if (Optional.ofNullable(pmWeatherList.get()).isPresent() && !pmWeatherList.get().isEmpty()) {
            for (DayOfAmPm7 day : area.getDayList()) {
                PmWeather oldPmWeather = pmWeatherList.get().get(0);
                newPmWeather.setPmWeather(oldPmWeather.getUid(), day);
                weatherDao.updateWeatherNode(newPmWeather);
            }
        } else {
            for (DayOfAmPm7 oneDay : area.getDayList()) {
                AtomicReference<Optional<PmWeather>> foundPmWeather = new AtomicReference<>(Optional.empty());
                Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundPmWeather.set(weatherDao.getPmWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, oneDay.getTm())));

                AtomicReference<String> pmWeatherUid = new AtomicReference<>();
                foundPmWeather.get().ifPresent(notNullPmWeather-> pmWeatherUid.set(notNullPmWeather.getUid()));

                newPmWeather = new PmWeather();
                newPmWeather.setPmWeather(pmWeatherUid.get(), oneDay);

                foundRegion.getPmWeathers().add(newPmWeather);
            }

            regionDao.updateRegionNode(foundRegion);

            for (DayOfAmPm7 oneDay : area.getDayList()) {
                AtomicReference<Optional<PmWeather>> foundPmWeather = new AtomicReference<>(Optional.empty());
                Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundPmWeather.set(weatherDao.getPmWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, oneDay.getTm())));

                Map<DateUnit, Integer> dateMap = dateService.splitDateIncludingDelim(oneDay.getTm());
                Optional<Day> dayNode = dateDao.getDayNode(dateMap);

                if(foundPmWeather.get().isPresent()) {
                    dayNode.ifPresent(day -> day.getPmWeathers().add(foundPmWeather.get().get()));
                    dateDao.updateDateNode(dayNode);
                }
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
                    Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();
                    String time = dateMap.get(DateUnit.HOUR).toString() + "00";

                    AtomicReference<Optional<HourlyWeather>> foundHourlyWeather1 = new AtomicReference<>(Optional.empty());
                    Optional.ofNullable(foundRegion.getUid())
                            .ifPresent(uid -> foundHourlyWeather1.set(weatherDao.getHourlyWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, area.getTm(), time, Query.HOURLY_WEATHER)));

                    AtomicReference<String> hourlyWeatherUid = new AtomicReference<>();
                    foundHourlyWeather1.get().ifPresent(notNullHourlyWeather-> hourlyWeatherUid.set(notNullHourlyWeather.getUid()));

                    newHourlyWeather = new HourlyWeather();
                    newHourlyWeather.setHourlyWeather(hourlyWeatherUid.get(), area);

                    foundRegion.getHourlyWeathers().add(newHourlyWeather);

                    regionDao.updateRegionNode(foundRegion);

                    AtomicReference<Optional<HourlyWeather>> foundHourlyWeather2 = new AtomicReference<>(Optional.empty());
                    Optional.ofNullable(foundRegion.getUid())
                            .ifPresent(uid -> foundHourlyWeather2.set(weatherDao.getHourlyWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, area.getTm(), time, Query.HOURLY_WEATHER)));

                    Optional<Hour> hourNode = dateDao.getHourNode(dateMap);

                    if(foundHourlyWeather2.get().isPresent()) {
                        hourNode.ifPresent(hour -> hour.getHourlyWeathers().add(foundHourlyWeather2.get().get()));
                        dateDao.updateDateNode(hourNode);
                    }
                }
            }
            log.info("[Service] callKweatherShkoApi - [" + area.getCode() + "] " + area.getAreaname1() + area.getAreaname2() + area.getAreaname3() + area.getAreaname4() + " 지역->날씨<-날짜 노드 연결 완료");
        }

    }

    private void checkAlreadyExistingWeatherNode(WeatherRootQuery weatherRootQuery, Regions foundRegion, String sidoName, String sggName, String umdName, RegionUnit regionUnit, DateUnit dateUnit, Query query) {
        weatherRootQuery.setWeatherRootQuery(weatherDao.getAlreadyExistingWeatherNodeWithRegionNameAndDate(sidoName, sggName, umdName, regionUnit, dateUnit, query));
        Optional.ofNullable(weatherRootQuery.getRegion()).ifPresent(region -> foundRegion.setRegionUidAndName(region.get(0), regionUnit));
    }

    public void callSpecialWeatherReportApi() {
        SpecialWeatherReportTopModel specialWeatherReportTopModel = new SpecialWeatherReportTopModel();

       String todayDate = dateService.makeCurrentDateFormat();
        URI uri = URI.create(specialWeatherReportApiUrl + "?ServiceKey=" + serviceKey + "&fromTmFc=" + todayDate + "&toTmFc=" + todayDate + "&_type=json");

        try {
            specialWeatherReportTopModel = restTemplate.getForObject(uri, SpecialWeatherReportTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }


        Optional<List<SpecialWeatherReport>> optionalSpecialWeatherReportList = Optional.ofNullable(specialWeatherReportTopModel)
                .map(SpecialWeatherReportTopModel::getResponse)
                .map(Response::getBody)
                .map(Body::getItems)
                .map(Items::getItem);

       //List<SpecialWeatherReport> weatherWarningList = weatherWarningTopModel.getResponse().getBody().getItems().getItem();

        Optional<Regions> optionalKorea = regionDao.getCountryNodeWithName(CountryList.KOREA.getCountryName());
        if(optionalKorea.isPresent()) {
            String countryUid = optionalKorea.get().getUid();

            if (optionalSpecialWeatherReportList.isPresent() && !optionalSpecialWeatherReportList.get().isEmpty()) {
                SpecialWeatherReport specialWeatherReport = optionalSpecialWeatherReportList.get().get(0);

                String date = specialWeatherReport.getTmFc().substring(0, 8);
                String time = specialWeatherReport.getTmFc().substring(8, 12);
                Map<DateUnit, Integer> dateMap = dateService.splitDateAndTime(date, time);

                WeatherRootQuery weatherRootQuery = new WeatherRootQuery();
                Regions foundCountry = new Regions();
                checkAlreadyExistingSpecialWeatherNode(weatherRootQuery, foundCountry, countryUid, dateMap, DateUnit.HOUR, Query.SPECIAL_WEATHER);

                List<SpecialWeather> specialWeatherList = weatherRootQuery.getSpecialWeather();

                SpecialWeather newSpecialWeather = new SpecialWeather();

                if(Optional.ofNullable(foundCountry.getUid()).isPresent()) {
                    if (Optional.ofNullable(specialWeatherList).isPresent() && !specialWeatherList.isEmpty()) {
                        SpecialWeather oldSpecialWeather = specialWeatherList.get(0);
                        newSpecialWeather.setSpecialWeather(oldSpecialWeather.getUid(), specialWeatherReport);
                        weatherDao.updateWeatherNode(newSpecialWeather);
                    } else {
                        AtomicReference<Optional<SpecialWeather>> foundSpecialWeather1 = new AtomicReference<>(Optional.empty());
                        Optional.ofNullable(foundCountry.getUid()).ifPresent(uid -> foundSpecialWeather1.set(weatherDao.getSpecialWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, date, time)));

                        AtomicReference<String> specialWeatherUid = new AtomicReference<>();
                        foundSpecialWeather1.get().ifPresent(notNullSpecialWeather -> specialWeatherUid.set(notNullSpecialWeather.getUid()));

                        newSpecialWeather = new SpecialWeather();
                        newSpecialWeather.setSpecialWeather(specialWeatherUid.get(), specialWeatherReport);

                        foundCountry.getSpecialWeathers().add(newSpecialWeather);
                        regionDao.updateRegionNode(foundCountry);

                        AtomicReference<Optional<SpecialWeather>> foundSpecialWeather2 = new AtomicReference<>(Optional.empty());
                        Optional.ofNullable(foundCountry.getUid()).ifPresent(uid -> foundSpecialWeather2.set(weatherDao.getSpecialWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, date, time)));

                        Optional<Hour> hourNode = dateDao.getHourNode(dateMap);

                        if (foundSpecialWeather2.get().isPresent()) {
                            hourNode.ifPresent(hour -> hour.getSpecialWeathers().add(foundSpecialWeather2.get().get()));
                            dateDao.updateDateNode(hourNode);
                        }
                    }
                }

            }
        }
    }

    private void checkAlreadyExistingSpecialWeatherNode(WeatherRootQuery weatherRootQuery, Regions foundCountry, String uid, Map<DateUnit, Integer> dateMap, DateUnit dateUnit, Query query) {
        weatherRootQuery.setWeatherRootQuery(weatherDao.getAlreadyExistingWeatherNodeWithRegionUidAndDate(uid, dateMap, query));
        Optional.ofNullable(weatherRootQuery.getRegion()).ifPresent(country -> foundCountry.setCountry(country.get(0)));
    }
}
