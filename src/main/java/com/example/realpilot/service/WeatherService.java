package com.example.realpilot.service;

import com.example.realpilot.dao.DateDao;
import com.example.realpilot.dao.RegionDao;
import com.example.realpilot.dao.WeatherDao;
import com.example.realpilot.exceptionList.ApiCallException;
import com.example.realpilot.externalApiModel.forecastGrib.*;
import com.example.realpilot.externalApiModel.forecastSpace.*;
import com.example.realpilot.externalApiModel.forecastTime.*;
import com.example.realpilot.externalApiModel.kweatherAmPm7.AreaOfAmPm7;
import com.example.realpilot.externalApiModel.kweatherAmPm7.DayOfAmPm7;
import com.example.realpilot.externalApiModel.kweatherAmPm7.KweatherAmPm7TopModel;
import com.example.realpilot.externalApiModel.kweatherDay7.AreaOfDay7;
import com.example.realpilot.externalApiModel.kweatherDay7.DayOfDay7;
import com.example.realpilot.externalApiModel.kweatherDay7.KweatherDay7TopModel;
import com.example.realpilot.externalApiModel.kweatherShko.AreaOfShko;
import com.example.realpilot.externalApiModel.kweatherShko.KweatherShkoTopModel;
import com.example.realpilot.externalApiModel.kweatherWorld.AreaOfWorld;
import com.example.realpilot.externalApiModel.kweatherWorld.DayOfWorld;
import com.example.realpilot.externalApiModel.kweatherWorld.KweatherWorldTopModel;
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
    @Value("${kweatherWorld.api.url}")
    private String kweatherWorldApiUrl;
    @Value("${specialWeather.api.url}")
    private String specialWeatherApiUrl;

    private Integer GRID_X_IDNEX = 0;
    private Integer GRID_Y_INDEX = 1;

    // 동네예보 API(초단기실황/초단기예보/동네예보)
    public void callWeatherApiOfKma() {
        ForecastGribTopModel forecastGribTopModel = new ForecastGribTopModel();
        ForecastTimeTopModel forecastTimeTopModel = new ForecastTimeTopModel();
        ForecastSpaceTopModel forecastSpaceTopModel = new ForecastSpaceTopModel();

        Optional<Set<Regions>> optionalGridList = regionDao.getGridList();

       for(Regions region : optionalGridList.get()) {
           Integer gridX = region.getGridX();
           Integer gridY = region.getGridY();

           List<Regions> regionList = regionDao.getRegionNodeWithGrid(gridX, gridY);

           String releaseDate = dateService.makeCurrentDateFormat();
           String releaseTime1 = dateService.makeCurrentTimeFormat(ExternalWeatherApi.FORECAST_GRIB);
           //releaseTime1 = "1100";
           String parameters1 = "?ServiceKey=" + serviceKey + "&base_date=" + releaseDate + "&base_time=" + releaseTime1 + "&nx=" + gridX + "&ny=" + gridY + "&numOfRows=300&_type=json";

          String releaseTime2 = dateService.makeCurrentTimeFormat(ExternalWeatherApi.FORECAST_SPACE);
           String parameters2 = "?ServiceKey=" + serviceKey + "&base_date=" + releaseDate + "&base_time=" + releaseTime2 + "&nx=" + gridX + "&ny=" + gridY + "&numOfRows=300&_type=json";

           URI forecastGribUri = URI.create(forecastGribApiUrl + parameters1);
           URI forecastTimeUri = URI.create(forecastTimeApiUrl + parameters1);
           URI forecastSpaceUri = URI.create(forecastSpaceApiUrl + parameters2);

           Map<DateUnit, Integer> dateMap = dateService.splitDateAndTime(releaseDate, releaseTime1);
           callForecastGribApi(forecastGribTopModel, forecastGribUri, releaseDate, releaseTime1, dateMap, regionList);
           callForecastTimeApi(forecastTimeTopModel, forecastTimeUri, releaseDate, releaseTime1, regionList);
           callForecastSpaceApi(forecastSpaceTopModel, forecastSpaceUri, releaseDate, releaseTime2, regionList);
       }
    }

    private void callForecastGribApi(ForecastGribTopModel forecastGribTopModel,URI forecastGribUri, String releaseDate, String releaseTime, Map<DateUnit, Integer> dateMap, List<Regions> regionList) {
        try {
            forecastGribTopModel = restTemplate.getForObject(forecastGribUri, ForecastGribTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiCallException();
        }

        Map<String, String> categoryValueMap = new HashMap<>();

        Optional<List<ForecastGrib>> optionalForecastGribList = Optional.ofNullable(forecastGribTopModel)
                .map(ForecastGribTopModel::getResponse)
                .map(ForecastGribResponse::getBody)
                .map(ForecastGribBody::getItems)
                .map(ForecastGribItems::getItem);

        if(optionalForecastGribList.isPresent() && !optionalForecastGribList.get().isEmpty()) {
            List<ForecastGrib> forecastGribList = optionalForecastGribList.get();

            for (ForecastGrib forecastGrib : forecastGribList) {
                /*log.info("[Service] callForecastGribApi - category : " + forecastGrib.getCategory());
                log.info("[Service] callForecastGribApi - value : " + forecastGrib.getObsrValue());*/

                categoryValueMap.put(forecastGrib.getCategory(), forecastGrib.getObsrValue());
            }

            HourlyWeather hourlyWeather = new HourlyWeather();
            hourlyWeather.setHourlyWeather(null, categoryValueMap, releaseDate, releaseTime);

            connectRegionAndHourlyWeatherAndDateNode(dateMap, releaseDate, releaseTime, regionList, hourlyWeather, ExternalWeatherApi.FORECAST_GRIB);
        }
    }

    private void callForecastTimeApi(ForecastTimeTopModel forecastTimeTopModel, URI forecastTimeUri, String releaseDate, String releaseTime, List<Regions> regionList) {
        try {
            forecastTimeTopModel = restTemplate.getForObject(forecastTimeUri, ForecastTimeTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiCallException();
        }

        int forecastTimeCount = getForecastTimeCount(releaseDate); // 예보시간의 구간 수 (2개 or 3개 or 4개)
        int index = 0;

        Map<String, String>[] categoryValueMapArray = new Map[4];
        for(int i=0 ; i < categoryValueMapArray.length ; ++i) {
            categoryValueMapArray[i] = new HashMap<>();
        }

        String forecastDate = "";
        String[] forecastTimeArray = new String[4];

        Optional<List<ForecastTime>> optionalForecastTimeList = Optional.ofNullable(forecastTimeTopModel)
                .map(ForecastTimeTopModel::getResponse)
                .map(ForecastTimeResponse::getBody)
                .map(ForecastTimeBody::getItems)
                .map(ForecastTimeItems::getItem);

        if(optionalForecastTimeList.isPresent() && !optionalForecastTimeList.get().isEmpty()) {
            List<ForecastTime> forecastTimeList = optionalForecastTimeList.get();

            for (ForecastTime forecastTime : forecastTimeList) {
                /*log.info("[Service] callForecastTimeApi - forecastTime : " + forecastTime.getFcstTime());
                log.info("[Service] callForecastTimeApi - category : " + forecastTime.getCategory());
                log.info("[Service] callForecastTimeApi - value : " + forecastTime.getFcstValue());*/

                // TODO: index 변수 대신에 현재 시간과 예보 시간의 차이(예보시간-현재시간-1)를 배열의 인덱스로 두어 저장하도록 수정
                forecastDate = forecastTime.getFcstDate();
                if (index % forecastTimeCount == 0) {
                    categoryValueMapArray[0].put(forecastTime.getCategory(), forecastTime.getFcstValue());
                    forecastTimeArray[0] = forecastTime.getFcstTime();
                } else if (index % forecastTimeCount == 1) {
                    categoryValueMapArray[1].put(forecastTime.getCategory(), forecastTime.getFcstValue());
                    forecastTimeArray[1] = forecastTime.getFcstTime();
                } else if (index % forecastTimeCount == 2) {
                    categoryValueMapArray[2].put(forecastTime.getCategory(), forecastTime.getFcstValue());
                    forecastTimeArray[2] = forecastTime.getFcstTime();
                } else if (index % forecastTimeCount == 3) {
                    categoryValueMapArray[3].put(forecastTime.getCategory(), forecastTime.getFcstValue());
                    forecastTimeArray[3] = forecastTime.getFcstTime();
                }
                ++index;
            }

            List<HourlyWeather> hourlyWeatherList = new ArrayList<>();

            // TODO: index 변수 대신에 현재 시간과 예보 시간의 차이(예보시간-현재시간-1)를 배열의 인덱스로 두어 저장하도록 수정
            for (int i = 0; i < forecastTimeCount; ++i) {
                HourlyWeather hourlyWeather = new HourlyWeather();
                if (i == 0) {
                    hourlyWeather.setHourlyWeather(null, categoryValueMapArray[0], releaseDate, releaseTime, forecastDate, forecastTimeArray[0]);
                } else if (i == 1) {
                    hourlyWeather.setHourlyWeather(null, categoryValueMapArray[1], releaseDate, releaseTime, forecastDate, forecastTimeArray[1]);
                } else if (i == 2) {
                    hourlyWeather.setHourlyWeather(null, categoryValueMapArray[2], releaseDate, releaseTime, forecastDate, forecastTimeArray[2]);
                } else if (i == 3) {
                    hourlyWeather.setHourlyWeather(null, categoryValueMapArray[3], releaseDate, releaseTime, forecastDate, forecastTimeArray[3]);
                }
                hourlyWeatherList.add(hourlyWeather);
            }

            for (HourlyWeather hourlyWeather : hourlyWeatherList) {
                String forecastTime = hourlyWeather.getForecastTime();
                Map<DateUnit, Integer> dateMap = dateService.splitDateAndTime(forecastDate, forecastTime);

                connectRegionAndHourlyWeatherAndDateNode(dateMap, forecastDate, forecastTime, regionList, hourlyWeather, ExternalWeatherApi.FORECAST_TIME);
            }
        }
    }

    private void callForecastSpaceApi(ForecastSpaceTopModel forecastSpaceTopModel, URI forecastSpaceUri, String releaseDate, String releaseTime, List<Regions> regionList) {
        try {
            forecastSpaceTopModel = restTemplate.getForObject(forecastSpaceUri, ForecastSpaceTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiCallException();
        }

        int index = 0;

        String prevForecastDate = "";
        String prevForecastTime = "";

        Map<String, String> categoryValueMap = new HashMap<>();
        List<Map<String, String>> categoryValueMapList = new ArrayList<>();
        List<HourlyWeather> hourlyWeatherList = new ArrayList<>();

        Optional<List<ForecastSpace>> optionalForecastSpaceList = Optional.ofNullable(forecastSpaceTopModel)
                .map(ForecastSpaceTopModel::getResponse)
                .map(ForecastSpaceResponse::getBody)
                .map(ForecastSpaceBody::getItems)
                .map(ForecastSpaceItems::getItem);

        if(optionalForecastSpaceList.isPresent() && !optionalForecastSpaceList.get().isEmpty()) {
            List<ForecastSpace> forecastSpaceList = optionalForecastSpaceList.get();

            for (ForecastSpace forecastSpace : forecastSpaceList) {
                HourlyWeather hourlyWeather = new HourlyWeather();

                if (!prevForecastTime.equals(forecastSpace.getFcstTime()) && index != 0) {
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

            for (HourlyWeather hourlyWeather : hourlyWeatherList) {
                String forecastDate = hourlyWeather.getForecastDate();
                String forecastTime = hourlyWeather.getForecastTime();

                Map<DateUnit, Integer> dateMap = dateService.splitDateAndTime(forecastDate, forecastTime);

                connectRegionAndHourlyWeatherAndDateNode(dateMap, forecastDate, forecastTime, regionList, hourlyWeather, ExternalWeatherApi.FORECAST_SPACE);
            }
        }
    }

    private void connectRegionAndHourlyWeatherAndDateNode(Map<DateUnit, Integer> dateMap, String date, String time, List<Regions> regionList, HourlyWeather newHourlyWeather, ExternalWeatherApi api) {
        Optional<Hour> hourNode = dateDao.getHourNode(dateMap);

        if (Optional.ofNullable(regionList).isPresent() && !regionList.isEmpty()) {
            for (Regions oneRegion : regionList) {
                Regions foundRegion = new Regions();
                WeatherRootQuery weatherRootQuery = new WeatherRootQuery();

                checkAlreadyExistingWeatherNodeOfKoreaByUid(weatherRootQuery, foundRegion, oneRegion.getUid(), dateMap, Query.HOURLY_WEATHER);

                List<HourlyWeather> hourlyWeatherList = weatherRootQuery.getHourlyWeather();

                if(Optional.ofNullable(foundRegion.getUid()).isPresent()) {
                    if (Optional.ofNullable(hourlyWeatherList).isPresent() && !hourlyWeatherList.isEmpty()) {
                        HourlyWeather oldHourlyWeather = hourlyWeatherList.get(0);
                        newHourlyWeather.setUid(oldHourlyWeather.getUid());
                        weatherDao.updateWeatherNode(newHourlyWeather);
                    } else {
                        foundRegion.setUid(oneRegion.getUid());

                        String hourlyWeatherUid = null;
                        if(Optional.ofNullable(weatherRootQuery.getRegion()).isPresent() && !weatherRootQuery.getRegion().isEmpty()) {
                            Regions region = weatherRootQuery.getRegion().get(0);
                            if(Optional.ofNullable(region.getHourlyWeathers()).isPresent() && !region.getHourlyWeathers().isEmpty()) {
                                hourlyWeatherUid = region.getHourlyWeathers().get(0).getUid();
                            }
                        }
                        newHourlyWeather.setUid(hourlyWeatherUid);

                        /*Optional<HourlyWeather> foundHourlyWeather1 = weatherDao.getHourlyWeatherNodeLinkedToRegionWithRegionUidAndDate(foundRegion.getUid(), date, time, Query.HOURLY_WEATHER);

                        AtomicReference<String> hourlyWeatherUid = new AtomicReference<>();
                        foundHourlyWeather1.ifPresent(notNullHourlyWeather -> hourlyWeatherUid.set(notNullHourlyWeather.getUid()));
                        newHourlyWeather.setUid(hourlyWeatherUid.get());*/

                        foundRegion.getHourlyWeathers().add(newHourlyWeather);
                        regionDao.updateRegionNode(foundRegion);

                        Optional<HourlyWeather> foundHourlyWeather = weatherDao.getHourlyWeatherNodeLinkedToRegionWithRegionUidAndDate(foundRegion.getUid(), date, time, Query.HOURLY_WEATHER);

                        if (Optional.ofNullable(foundHourlyWeather).isPresent()) {
                            Optional<HourlyWeather> finalFoundHourlyWeather = foundHourlyWeather;
                            hourNode.ifPresent(hour -> hour.getHourlyWeathers().add(finalFoundHourlyWeather.get()));
                        }
                    }
                }
            }
        }
        dateDao.updateDateNode(hourNode);

        hourNode.ifPresent(hour -> log.info("[Service] connectRegionAndHourlyWeatherAndDateNode - [" + api.toString() + "] " + hour.getHour() + "시의 " + "지역->날씨<-날짜 노드 연결 완료"));
    }

    private void checkAlreadyExistingWeatherNodeOfKoreaByUid(WeatherRootQuery weatherRootQuery, Regions foundRegion, String uid, Map<DateUnit, Integer> dateMap, Query query) {
        weatherRootQuery.setWeatherRootQuery(weatherDao.getAlreadyExistingWeatherNodeWithRegionUidAndDate(uid, dateMap, query));
        if(Optional.ofNullable(weatherRootQuery.getRegion()).isPresent() && !weatherRootQuery.getRegion().isEmpty()) {
            foundRegion.setRegion(weatherRootQuery.getRegion().get(0));
        }
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
        KweatherWorldTopModel kweatherWorldTopModel = new KweatherWorldTopModel();

        URI kweatherDay7Uri = URI.create(kweatherDay7ApiUrl);
        URI kweatherAmPm7Uri = URI.create(kweatherAmPm7ApiUrl);
        URI kweatherShkoUri = URI.create(kweatherShkoApiUrl);
        URI kweatherWorldUri = URI.create(kweatherWorldApiUrl);

        Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();

        callKweatherDay7Api(kweatherDay7TopModel, kweatherDay7Uri);
        /*callKweatherAmPm7Api(kweatherAmPm7TopModel, kweatherAmPm7Uri);
        callKweatherShkoApi(kweatherShkoTopModel, kweatherShkoUri, dateMap);
        callWorldWeatherApiOfKweather(kweatherWorldTopModel, kweatherWorldUri);*/
    }


    private void callKweatherDay7Api(KweatherDay7TopModel kweatherDay7TopModel, URI kweatherDay7Uri) {
        try {
            kweatherDay7TopModel = restTemplate.getForObject(kweatherDay7Uri, KweatherDay7TopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiCallException();
        }

        // TODO: TopModel도 Optional 사용
        for (AreaOfDay7 area : kweatherDay7TopModel.getAreas()) {
            Regions foundRegion = new Regions();

            String sidoName = "";
            String sggName = "";
            String umdName = "";

            for (DayOfDay7 day : area.getDayList()) {
                Map<DateUnit, Integer> dateMap = dateService.splitDateIncludingDelim(day.getTm());

                WeatherRootQuery weatherRootQuery = new WeatherRootQuery();

                if (area.getAreaname1().equals(ConnectedSidoUmd.SEJONG.getSidoName())) {
                    sidoName = area.getAreaname1();
                    umdName = area.getAreaname2();
                    checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_UMD, DateUnit.DAY, Query.DAILY_WEATHER);
                } else {
                    if (area.getAreaname1().equals("-") || area.getAreaname1().equals("NA")) {
                        sidoName = area.getAreaname2();
                        checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO, DateUnit.DAY, Query.DAILY_WEATHER);
                    } else if (area.getAreaname2().equals("NA")) {
                        sidoName = area.getAreaname1();
                        checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO, DateUnit.DAY, Query.DAILY_WEATHER);
                    } else {
                        sidoName = area.getAreaname1();
                        sggName = area.getAreaname2() + " " + area.getAreaname3();
                        sggName = sggName.replaceAll("-|NA", "");

                        if (area.getAreaname3().equals("-") || area.getAreaname3().equals("NA")) {
                            sggName = sggName.replaceAll(" ", "");
                        }

                        if (area.getAreaname4().equals("-") || area.getAreaname4().equals("NA")) {
                            checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_SGG, DateUnit.DAY, Query.DAILY_WEATHER);
                        } else if (!area.getAreaname4().equals("-") && !area.getAreaname4().equals("NA")) {
                            umdName = area.getAreaname4();
                            checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_SGG_UMD, DateUnit.DAY, Query.DAILY_WEATHER);
                        }
                    }
                }

                List<DailyWeather> dailyWeatherList = weatherRootQuery.getDailyWeather();

                if (Optional.ofNullable(foundRegion.getUid()).isPresent()) {
                    DailyWeather newDailyWeather = new DailyWeather();

                    if (Optional.ofNullable(dailyWeatherList).isPresent() && !dailyWeatherList.isEmpty()) {
                        DailyWeather oldDailyWeather = dailyWeatherList.get(0);
                        newDailyWeather.setDailyWeather(oldDailyWeather.getUid(), day);
                        weatherDao.updateWeatherNode(newDailyWeather);
                    } else {
                        String dailyWeatherUid = null;
                        if (Optional.ofNullable(foundRegion.getDailyWeathers()).isPresent() && !foundRegion.getDailyWeathers().isEmpty()) {
                            dailyWeatherUid = foundRegion.getHourlyWeathers().get(0).getUid();
                        }
                        newDailyWeather.setUid(dailyWeatherUid);

                        newDailyWeather = new DailyWeather();
                        newDailyWeather.setDailyWeather(dailyWeatherUid, day);

                        foundRegion.getDailyWeathers().add(newDailyWeather);

                        regionDao.updateRegionNode(foundRegion);

                        AtomicReference<Optional<DailyWeather>> foundDailyWeather = new AtomicReference<>(Optional.empty());
                        Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundDailyWeather.set(weatherDao.getDailyWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, day.getTm().replaceAll("/", ""))));

                        Optional<Day> dayNode = dateDao.getDayNode(dateMap);

                        if (foundDailyWeather.get().isPresent()) {
                            dayNode.ifPresent(notNullDay -> notNullDay.getDailyWeathers().add(foundDailyWeather.get().get()));
                            dateDao.updateDateNode(dayNode);
                        }
                        dayNode.ifPresent(notNullDay -> log.info("[Service] callKweatherDay7Api - " + notNullDay.getDay() + "일의 " + "지역->날씨<-날짜 노드 연결 완료"));
                    }
                }
                foundRegion.getDailyWeathers().clear();
            }
            log.info("[Service] callKweatherDay7Api - 지역 : " + "[" + area.getCode() + "] " + area.getAreaname1() + area.getAreaname2() + area.getAreaname3() + area.getAreaname4());
        }
    }

    private void callKweatherAmPm7Api(KweatherAmPm7TopModel kweatherAmPm7TopModel, URI kweatherAmPm7Uri) {
        try {
            kweatherAmPm7TopModel = restTemplate.getForObject(kweatherAmPm7Uri, KweatherAmPm7TopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiCallException();
        }

        for (AreaOfAmPm7 area : kweatherAmPm7TopModel.getAreas()) {
            Regions foundRegion = new Regions();

            String sidoName = "";
            String sggName = "";
            String umdName = "";

            for (DayOfAmPm7 day : area.getDayList()) {
                Map<DateUnit, Integer> dateMap = dateService.splitDateIncludingDelim(day.getTm());

                WeatherRootQuery weatherRootQueryForAm = new WeatherRootQuery();
                WeatherRootQuery weatherRootQueryForPm = new WeatherRootQuery();

                Optional<RegionUnit> regionUnit = Optional.empty();

                if (area.getAreaname1().equals(ConnectedSidoUmd.SEJONG.getSidoName())) {
                    sidoName = area.getAreaname1();
                    umdName = area.getAreaname2();
                    checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQueryForAm, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_UMD, DateUnit.DAY, Query.AM_WEATHER);
                    weatherRootQueryForPm = weatherDao.getAlreadyExistingWeatherNodeOfKoreaWithRegionNameAndDate(sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_UMD, DateUnit.DAY, Query.PM_WEATHER);
                    regionUnit = Optional.of(RegionUnit.SIDO_UMD);
                } else {
                    if (area.getAreaname1().equals("-") || area.getAreaname1().equals("NA")) {
                        sidoName = area.getAreaname2();
                        checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQueryForAm, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO, DateUnit.DAY, Query.AM_WEATHER);
                        weatherRootQueryForPm = weatherDao.getAlreadyExistingWeatherNodeOfKoreaWithRegionNameAndDate(sidoName, sggName, umdName, dateMap, RegionUnit.SIDO, DateUnit.DAY, Query.PM_WEATHER);
                        regionUnit = Optional.of(RegionUnit.SIDO);
                    } else if (area.getAreaname2().equals("NA")) {
                        sidoName = area.getAreaname1();
                        checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQueryForAm, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO, DateUnit.DAY, Query.AM_WEATHER);
                        weatherRootQueryForPm = weatherDao.getAlreadyExistingWeatherNodeOfKoreaWithRegionNameAndDate(sidoName, sggName, umdName, dateMap, RegionUnit.SIDO, DateUnit.DAY, Query.PM_WEATHER);
                        regionUnit = Optional.of(RegionUnit.SIDO);

                    } else {
                        sidoName = area.getAreaname1();
                        sggName = area.getAreaname2() + " " + area.getAreaname3();
                        sggName = sggName.replaceAll("-|NA", "");

                        if (area.getAreaname3().equals("-") || area.getAreaname3().equals("NA")) {
                            sggName = sggName.replaceAll(" ", "");
                        }

                        if (area.getAreaname4().equals("-") || area.getAreaname4().equals("NA")) {
                            checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQueryForAm, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_SGG, DateUnit.DAY, Query.AM_WEATHER);
                            weatherRootQueryForPm = weatherDao.getAlreadyExistingWeatherNodeOfKoreaWithRegionNameAndDate(sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_SGG, DateUnit.DAY, Query.PM_WEATHER);
                            regionUnit = Optional.of(RegionUnit.SIDO_SGG);
                        } else if (!area.getAreaname4().equals("-") && !area.getAreaname4().equals("NA")) {
                            umdName = area.getAreaname4();
                            checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQueryForAm, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_SGG_UMD, DateUnit.DAY, Query.AM_WEATHER);
                            weatherRootQueryForPm = weatherDao.getAlreadyExistingWeatherNodeOfKoreaWithRegionNameAndDate(sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_SGG_UMD, DateUnit.DAY, Query.PM_WEATHER);
                            regionUnit = Optional.of(RegionUnit.SIDO_SGG_UMD);
                        }
                    }
                }

                AtomicReference<List<AmWeather>> amWeatherList = new AtomicReference<>();
                Optional.ofNullable(weatherRootQueryForAm.getAmWeather()).ifPresent(amWeather -> amWeatherList.set(amWeather));
                AtomicReference<List<PmWeather>> pmWeatherList = new AtomicReference<>();
                Optional.ofNullable(weatherRootQueryForPm.getPmWeather()).ifPresent(pmWeather -> pmWeatherList.set(pmWeather));

                if (Optional.ofNullable(foundRegion.getUid()).isPresent()) {
                    Regions prevFoundRegion = new Regions();
                    Optional.ofNullable(regionUnit).ifPresent(foundRegionUnit -> prevFoundRegion.setRegionUidAndName(foundRegion, foundRegionUnit.get()));
                    connectRegionAndAmWeatherAndDateNode(day, foundRegion, dateMap, amWeatherList);
                    connectRegionAndPmWeatherAndDateNode(day, prevFoundRegion, dateMap, pmWeatherList);
                }
                log.info("[Service] callKweatherAmPm7Api - 지역 : " + "[" + area.getCode() + "] " + area.getAreaname1() + area.getAreaname2() + area.getAreaname3() + area.getAreaname4());
            }
        }
    }

    private void connectRegionAndAmWeatherAndDateNode(DayOfAmPm7 day, Regions foundRegion, Map<DateUnit, Integer> dateMap, AtomicReference<List<AmWeather>> amWeatherList) {
        AmWeather newAmWeather = new AmWeather();

        if (Optional.ofNullable(amWeatherList.get()).isPresent() && !amWeatherList.get().isEmpty()) {
            AmWeather oldAmWeather = amWeatherList.get().get(0);
            newAmWeather.setAmWeather(oldAmWeather.getUid(), day);
            weatherDao.updateWeatherNode(newAmWeather);
        } else {
            String amWeatherUid = null;
            if (Optional.ofNullable(foundRegion.getAmWeathers()).isPresent() && !foundRegion.getAmWeathers().isEmpty()) {
                amWeatherUid = foundRegion.getAmWeathers().get(0).getUid();
            }
            newAmWeather.setUid(amWeatherUid);

            newAmWeather = new AmWeather();
            newAmWeather.setAmWeather(amWeatherUid, day);

            foundRegion.getAmWeathers().add(newAmWeather);

            regionDao.updateRegionNode(foundRegion);

            AtomicReference<Optional<AmWeather>> foundAmWeather = new AtomicReference<>(Optional.empty());
            Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundAmWeather.set(weatherDao.getAmWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, day.getTm().replaceAll("/", ""))));

            Optional<Day> dayNode = dateDao.getDayNode(dateMap);

            if(foundAmWeather.get().isPresent()) {
                dayNode.ifPresent(notNullDay -> notNullDay.getAmWeathers().add(foundAmWeather.get().get()));
                dateDao.updateDateNode(dayNode);
            }
            dayNode.ifPresent(notNullDay -> log.info("[Service] connectRegionAndAmWeatherAndDateNode - [AM] " + notNullDay.getDay() + "일의 " + "지역->날씨<-날짜 노드 연결 완료"));
        }
    }

    private void connectRegionAndPmWeatherAndDateNode(DayOfAmPm7 day, Regions foundRegion, Map<DateUnit, Integer> dateMap, AtomicReference<List<PmWeather>> pmWeatherList) {
        PmWeather newPmWeather = new PmWeather();

        if (Optional.ofNullable(pmWeatherList.get()).isPresent() && !pmWeatherList.get().isEmpty()) {
            PmWeather oldPmWeather = pmWeatherList.get().get(0);
            newPmWeather.setPmWeather(oldPmWeather.getUid(), day);
            weatherDao.updateWeatherNode(newPmWeather);
        } else {
            String pmWeatherUid = null;
            if (Optional.ofNullable(foundRegion.getPmWeathers()).isPresent() && !foundRegion.getPmWeathers().isEmpty()) {
                pmWeatherUid = foundRegion.getPmWeathers().get(0).getUid();
            }
            newPmWeather.setUid(pmWeatherUid);

            newPmWeather = new PmWeather();
            newPmWeather.setPmWeather(pmWeatherUid, day);

            foundRegion.getPmWeathers().add(newPmWeather);

            regionDao.updateRegionNode(foundRegion);

            AtomicReference<Optional<PmWeather>> foundPmWeather = new AtomicReference<>(Optional.empty());
            Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundPmWeather.set(weatherDao.getPmWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, day.getTm().replaceAll("/", ""))));

            Optional<Day> dayNode = dateDao.getDayNode(dateMap);

            if(foundPmWeather.get().isPresent()) {
                dayNode.ifPresent(notNulDay -> notNulDay.getPmWeathers().add(foundPmWeather.get().get()));
                dateDao.updateDateNode(dayNode);
            }
            dayNode.ifPresent(notNulDay -> log.info("[Service] connectRegionAndPmWeatherAndDateNode - [PM] " + notNulDay.getDay() + "일의 " + "지역->날씨<-날짜 노드 연결 완료"));
        }
    }

    private void callKweatherShkoApi(KweatherShkoTopModel kweatherShkoTopModel, URI kweatherShkoUri, Map<DateUnit, Integer> dateMap) {
        try {
            kweatherShkoTopModel = restTemplate.getForObject(kweatherShkoUri, KweatherShkoTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiCallException();
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
                checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_UMD, DateUnit.HOUR, Query.HOURLY_WEATHER);
            } else {
                if (area.getAreaname1().equals("-") || area.getAreaname1().equals("NA")) {
                    sidoName = area.getAreaname2();
                    checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO, DateUnit.HOUR, Query.HOURLY_WEATHER);
                } else if(area.getAreaname2().equals("NA")) {
                    sidoName = area.getAreaname1();
                    checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO, DateUnit.HOUR, Query.HOURLY_WEATHER);
                } else {
                    sidoName = area.getAreaname1();
                    sggName = area.getAreaname2() + " " + area.getAreaname3();
                    sggName = sggName.replaceAll("-|NA", "");

                    if(area.getAreaname3().equals("-") || area.getAreaname3().equals("NA")) {
                        sggName = sggName.replaceAll(" ", "");
                    }

                    if (area.getAreaname4().equals("-") || area.getAreaname4().equals("NA")) {
                        checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_SGG, DateUnit.HOUR, Query.HOURLY_WEATHER);
                    } else if(!area.getAreaname4().equals("-") && !area.getAreaname4().equals("NA")){
                        umdName = area.getAreaname4();
                        checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_SGG_UMD, DateUnit.HOUR, Query.HOURLY_WEATHER);
                    }
                }
            }

            List<HourlyWeather> hourlyWeatherList = weatherRootQuery.getHourlyWeather();

            if(Optional.ofNullable(foundRegion.getUid()).isPresent()) {
                HourlyWeather newHourlyWeather = new HourlyWeather();

                if (Optional.ofNullable(hourlyWeatherList).isPresent() && !hourlyWeatherList.isEmpty()) {
                    HourlyWeather oldHourlyWeather = hourlyWeatherList.get(0);
                    newHourlyWeather.setHourlyWeather(oldHourlyWeather.getUid(), area, dateMap);
                    weatherDao.updateWeatherNode(newHourlyWeather);
                } else {
                    String hourlyWeatherUid = null;
                    if (Optional.ofNullable(foundRegion.getHourlyWeathers()).isPresent() && !foundRegion.getHourlyWeathers().isEmpty()) {
                        hourlyWeatherUid = foundRegion.getHourlyWeathers().get(0).getUid();
                    }
                    newHourlyWeather.setUid(hourlyWeatherUid);

                    newHourlyWeather = new HourlyWeather();
                    newHourlyWeather.setHourlyWeather(hourlyWeatherUid, area, dateMap);

                    foundRegion.getHourlyWeathers().add(newHourlyWeather);

                    regionDao.updateRegionNode(foundRegion);

                    String time = dateMap.get(DateUnit.HOUR).toString() + "00";
                    AtomicReference<Optional<HourlyWeather>> foundHourlyWeather = new AtomicReference<>(Optional.empty());
                    Optional.ofNullable(foundRegion.getUid())
                            .ifPresent(uid -> foundHourlyWeather.set(weatherDao.getHourlyWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, area.getTm(), time, Query.HOURLY_WEATHER)));

                    Optional<Hour> hourNode = dateDao.getHourNode(dateMap);

                    if(foundHourlyWeather.get().isPresent()) {
                        hourNode.ifPresent(hour -> hour.getHourlyWeathers().add(foundHourlyWeather.get().get()));
                        dateDao.updateDateNode(hourNode);
                    }
                }
            }
            log.info("[Service] callKweatherShkoApi - [" + area.getCode() + "] " + area.getAreaname1() + area.getAreaname2() + area.getAreaname3() + area.getAreaname4() + " 지역->날씨<-날짜 노드 연결 완료");
        }

    }

   /* private void callKweatherDay7Api(KweatherDay7TopModel kweatherDay7TopModel, URI kweatherDay7Uri, Map<DateUnit, Integer> dateMap) {
        try {
            kweatherDay7TopModel = restTemplate.getForObject(kweatherDay7Uri, KweatherDay7TopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiCallException();
        }

        // TODO: TopModel도 Optional 사용
        for (AreaOfDay7 area : kweatherDay7TopModel.getAreas()) {
            Regions foundRegion = new Regions();

            String sidoName = "";
            String sggName = "";
            String umdName = "";

            //dateMap = dateService.splitDateIncludingDelim(area.);

            WeatherRootQuery weatherRootQuery = new WeatherRootQuery();

            if(area.getAreaname1().equals(ConnectedSidoUmd.SEJONG.getSidoName())) {
                sidoName = area.getAreaname1();
                umdName = area.getAreaname2();
                checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_UMD, DateUnit.DAY, Query.DAILY_WEATHER);
            } else {
                if (area.getAreaname1().equals("-") || area.getAreaname1().equals("NA")) {
                    sidoName = area.getAreaname2();
                    checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO, DateUnit.DAY, Query.DAILY_WEATHER);
                } else if(area.getAreaname2().equals("NA")) {
                    sidoName = area.getAreaname1();
                    checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO, DateUnit.DAY, Query.DAILY_WEATHER);
                } else {
                    sidoName = area.getAreaname1();
                    sggName = area.getAreaname2() + " " + area.getAreaname3();
                    sggName = sggName.replaceAll("-|NA", "");

                    if(area.getAreaname3().equals("-") || area.getAreaname3().equals("NA")) {
                        sggName = sggName.replaceAll(" ", "");
                    }

                    if (area.getAreaname4().equals("-") || area.getAreaname4().equals("NA")) {
                        checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_SGG, DateUnit.DAY, Query.DAILY_WEATHER);
                    } else if(!area.getAreaname4().equals("-") && !area.getAreaname4().equals("NA")){
                        umdName = area.getAreaname4();
                        checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_SGG_UMD, DateUnit.DAY, Query.DAILY_WEATHER);
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
                        //
                        String dailyWeatherUid = null;
                        if(Optional.ofNullable(weatherRootQuery.getRegion()).isPresent() && !weatherRootQuery.getRegion().isEmpty()) {
                            Regions region = weatherRootQuery.getRegion().get(0);
                            if(Optional.ofNullable(region.getDailyWeathers()).isPresent() && !region.getDailyWeathers().isEmpty()) {
                                dailyWeatherUid = region.getHourlyWeathers().get(0).getUid();
                            }
                        }
                        newDailyWeather.setUid(dailyWeatherUid);
                        //


                        *//*AtomicReference<Optional<DailyWeather>> foundDailyWeather = new AtomicReference<>(Optional.empty());
                        Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundDailyWeather.set(weatherDao.getDailyWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, oneDay.getTm().replaceAll("/", ""))));

                        AtomicReference<String> dailyWeatherUid = new AtomicReference<>();
                        foundDailyWeather.get().ifPresent(notNullDailyWeather-> dailyWeatherUid.set(notNullDailyWeather.getUid()));*//*

                        newDailyWeather = new DailyWeather();
                        newDailyWeather.setDailyWeather(dailyWeatherUid.get(), oneDay);

                        foundRegion.getDailyWeathers().add(newDailyWeather);
                    }

                    regionDao.updateRegionNode(foundRegion);

                    for (DayOfDay7 oneDay : area.getDayList()) {
                        AtomicReference<Optional<DailyWeather>> foundDailyWeather = new AtomicReference<>(Optional.empty());
                        Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundDailyWeather.set(weatherDao.getDailyWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, oneDay.getTm().replaceAll("/", ""))));

                        Map<DateUnit, Integer> forecastDateMap = dateService.splitDateIncludingDelim(oneDay.getTm());
                        Optional<Day> dayNode = dateDao.getDayNode(forecastDateMap);

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
    }*/

   /* private void callKweatherAmPm7Api(KweatherAmPm7TopModel kweatherAmPm7TopModel, URI kweatherAmPm7Uri, Map<DateUnit, Integer> dateMap) {
        try {
            kweatherAmPm7TopModel = restTemplate.getForObject(kweatherAmPm7Uri, KweatherAmPm7TopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiCallException();
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
                checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQueryForAm, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_UMD, DateUnit.DAY, Query.AM_WEATHER);
                weatherRootQueryForPm = weatherDao.getAlreadyExistingWeatherNodeOfKoreaWithRegionNameAndDate(sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_UMD, DateUnit.DAY, Query.PM_WEATHER);
                regionUnit = Optional.of(RegionUnit.SIDO_UMD);
            } else {
                if (area.getAreaname1().equals("-") || area.getAreaname1().equals("NA")) {
                    sidoName = area.getAreaname2();
                    checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQueryForAm, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO, DateUnit.DAY, Query.AM_WEATHER);
                    weatherRootQueryForPm = weatherDao.getAlreadyExistingWeatherNodeOfKoreaWithRegionNameAndDate(sidoName, sggName, umdName, dateMap, RegionUnit.SIDO, DateUnit.DAY, Query.PM_WEATHER);
                    regionUnit = Optional.of(RegionUnit.SIDO);
                } else if(area.getAreaname2().equals("NA")) {
                    sidoName = area.getAreaname1();
                    checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQueryForAm, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO, DateUnit.DAY, Query.AM_WEATHER);
                    weatherRootQueryForPm = weatherDao.getAlreadyExistingWeatherNodeOfKoreaWithRegionNameAndDate(sidoName, sggName, umdName, dateMap, RegionUnit.SIDO, DateUnit.DAY, Query.PM_WEATHER);
                    regionUnit = Optional.of(RegionUnit.SIDO);

                } else {
                    sidoName = area.getAreaname1();
                    sggName = area.getAreaname2() + " " + area.getAreaname3();
                    sggName = sggName.replaceAll("-|NA", "");

                    if(area.getAreaname3().equals("-") || area.getAreaname3().equals("NA")) {
                        sggName = sggName.replaceAll(" ", "");
                    }

                    if (area.getAreaname4().equals("-") || area.getAreaname4().equals("NA")) {
                        checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQueryForAm, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_SGG, DateUnit.DAY, Query.AM_WEATHER);
                        weatherRootQueryForPm = weatherDao.getAlreadyExistingWeatherNodeOfKoreaWithRegionNameAndDate(sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_SGG, DateUnit.DAY, Query.PM_WEATHER);
                        regionUnit = Optional.of(RegionUnit.SIDO_SGG);
                    } else if(!area.getAreaname4().equals("-") && !area.getAreaname4().equals("NA")){
                        umdName = area.getAreaname4();
                        checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQueryForAm, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_SGG_UMD, DateUnit.DAY, Query.AM_WEATHER);
                        weatherRootQueryForPm = weatherDao.getAlreadyExistingWeatherNodeOfKoreaWithRegionNameAndDate(sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_SGG_UMD, DateUnit.DAY, Query.PM_WEATHER);
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
    */

    /*private void connectRegionAndAmWeatherAndDateNode(AreaOfAmPm7 area, Regions foundRegion, AtomicReference<List<AmWeather>> amWeatherList) {
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
                Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundAmWeather.set(weatherDao.getAmWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, oneDay.getTm().replaceAll("/", ""))));

                AtomicReference<String> amWeatherUid = new AtomicReference<>();
                foundAmWeather.get().ifPresent(notNullAmWeather-> amWeatherUid.set(notNullAmWeather.getUid()));

                newAmWeather = new AmWeather();
                newAmWeather.setAmWeather(amWeatherUid.get(), oneDay);

                foundRegion.getAmWeathers().add(newAmWeather);
            }

            regionDao.updateRegionNode(foundRegion);

            for (DayOfAmPm7 oneDay : area.getDayList()) {
                AtomicReference<Optional<AmWeather>> foundAmWeather = new AtomicReference<>(Optional.empty());
                Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundAmWeather.set(weatherDao.getAmWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, oneDay.getTm().replaceAll("/", ""))));

                Map<DateUnit, Integer> forecastDateMap = dateService.splitDateIncludingDelim(oneDay.getTm());
                Optional<Day> dayNode = dateDao.getDayNode(forecastDateMap);

                if(foundAmWeather.get().isPresent()) {
                    dayNode.ifPresent(day -> day.getAmWeathers().add(foundAmWeather.get().get()));
                    dateDao.updateDateNode(dayNode);
                }
                dayNode.ifPresent(day -> log.info("[Service] connectRegionAndAmWeatherAndDateNode - [AM] " + day.getDay() + "일의 " + "지역->날씨<-날짜 노드 연결 완료"));
            }
        }
    }*/

    /*private void connectRegionAndPmWeatherAndDateNode(AreaOfAmPm7 area, Regions foundRegion, AtomicReference<List<PmWeather>> pmWeatherList) {
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
                Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundPmWeather.set(weatherDao.getPmWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, oneDay.getTm().replaceAll("/", ""))));

                AtomicReference<String> pmWeatherUid = new AtomicReference<>();
                foundPmWeather.get().ifPresent(notNullPmWeather-> pmWeatherUid.set(notNullPmWeather.getUid()));

                newPmWeather = new PmWeather();
                newPmWeather.setPmWeather(pmWeatherUid.get(), oneDay);

                foundRegion.getPmWeathers().add(newPmWeather);
            }

            regionDao.updateRegionNode(foundRegion);

            for (DayOfAmPm7 oneDay : area.getDayList()) {
                AtomicReference<Optional<PmWeather>> foundPmWeather = new AtomicReference<>(Optional.empty());
                Optional.ofNullable(foundRegion.getUid()).ifPresent(uid -> foundPmWeather.set(weatherDao.getPmWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, oneDay.getTm().replaceAll("/", ""))));

                Map<DateUnit, Integer> dateMap = dateService.splitDateIncludingDelim(oneDay.getTm());
                Optional<Day> dayNode = dateDao.getDayNode(dateMap);

                if(foundPmWeather.get().isPresent()) {
                    dayNode.ifPresent(day -> day.getPmWeathers().add(foundPmWeather.get().get()));
                    dateDao.updateDateNode(dayNode);
                }
                dayNode.ifPresent(day -> log.info("[Service] connectRegionAndPmWeatherAndDateNode - [PM] " + day.getDay() + "일의 " + "지역->날씨<-날짜 노드 연결 완료"));
            }
        }
    }*/

    /*private void callKweatherShkoApi(KweatherShkoTopModel kweatherShkoTopModel, URI kweatherShkoUri, Map<DateUnit, Integer> dateMap) {
        try {
            kweatherShkoTopModel = restTemplate.getForObject(kweatherShkoUri, KweatherShkoTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiCallException();
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
                checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_UMD, DateUnit.HOUR, Query.HOURLY_WEATHER);
            } else {
                if (area.getAreaname1().equals("-") || area.getAreaname1().equals("NA")) {
                    sidoName = area.getAreaname2();
                    checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO, DateUnit.HOUR, Query.HOURLY_WEATHER);
                } else if(area.getAreaname2().equals("NA")) {
                    sidoName = area.getAreaname1();
                    checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO, DateUnit.HOUR, Query.HOURLY_WEATHER);
                } else {
                    sidoName = area.getAreaname1();
                    sggName = area.getAreaname2() + " " + area.getAreaname3();
                    sggName = sggName.replaceAll("-|NA", "");

                    if(area.getAreaname3().equals("-") || area.getAreaname3().equals("NA")) {
                        sggName = sggName.replaceAll(" ", "");
                    }

                    if (area.getAreaname4().equals("-") || area.getAreaname4().equals("NA")) {
                        checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_SGG, DateUnit.HOUR, Query.HOURLY_WEATHER);
                    } else if(!area.getAreaname4().equals("-") && !area.getAreaname4().equals("NA")){
                        umdName = area.getAreaname4();
                        checkAlreadyExistingWeatherNodeOfKoreaByName(weatherRootQuery, foundRegion, sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_SGG_UMD, DateUnit.HOUR, Query.HOURLY_WEATHER);
                    }
                }
            }

            List<HourlyWeather> hourlyWeatherList = weatherRootQuery.getHourlyWeather();

            if(Optional.ofNullable(foundRegion.getUid()).isPresent()) {
                HourlyWeather newHourlyWeather = new HourlyWeather();

                if (Optional.ofNullable(hourlyWeatherList).isPresent() && !hourlyWeatherList.isEmpty()) {
                    HourlyWeather oldHourlyWeather = hourlyWeatherList.get(0);
                    newHourlyWeather.setHourlyWeather(oldHourlyWeather.getUid(), area, dateMap);
                    weatherDao.updateWeatherNode(newHourlyWeather);
                } else {
                    String time = dateMap.get(DateUnit.HOUR).toString() + "00";

                    AtomicReference<Optional<HourlyWeather>> foundHourlyWeather1 = new AtomicReference<>(Optional.empty());
                    Optional.ofNullable(foundRegion.getUid())
                            .ifPresent(uid -> foundHourlyWeather1.set(weatherDao.getHourlyWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, area.getTm(), time, Query.HOURLY_WEATHER)));

                    AtomicReference<String> hourlyWeatherUid = new AtomicReference<>();
                    foundHourlyWeather1.get().ifPresent(notNullHourlyWeather-> hourlyWeatherUid.set(notNullHourlyWeather.getUid()));

                    newHourlyWeather = new HourlyWeather();
                    newHourlyWeather.setHourlyWeather(hourlyWeatherUid.get(), area, dateMap);

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

    }*/

    private void checkAlreadyExistingWeatherNodeOfKoreaByName(WeatherRootQuery weatherRootQuery, Regions foundRegion, String sidoName, String sggName, String umdName, Map<DateUnit, Integer> dateMap, RegionUnit regionUnit, DateUnit dateUnit, Query query) {
        weatherRootQuery.setWeatherRootQuery(weatherDao.getAlreadyExistingWeatherNodeOfKoreaWithRegionNameAndDate(sidoName, sggName, umdName, dateMap, regionUnit, dateUnit, query));
        if(Optional.ofNullable(weatherRootQuery.getRegion()).isPresent() && !weatherRootQuery.getRegion().isEmpty()) {
            foundRegion.setRegionUidAndName(weatherRootQuery.getRegion().get(0), regionUnit);
        }
    }

    public void callWorldWeatherApiOfKweather(KweatherWorldTopModel kweatherWorldTopModel, URI kweatherWorldUri) {
        try {
            kweatherWorldTopModel = restTemplate.getForObject(kweatherWorldUri, KweatherWorldTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiCallException();
        }

        for (AreaOfWorld area : kweatherWorldTopModel.getAreas()) {
            WeatherRootQuery weatherRootQuery = new WeatherRootQuery();
            Regions foundCountry = new Regions();

            for (DayOfWorld day : area.getDayList()) {
                Map<DateUnit, Integer> dateMap = dateService.splitDateIncludingDelim(day.getTm());

                String cityName = area.getDay0().getCity();
                checkAlreadyExistingWeatherNodeOfWorld(weatherRootQuery, foundCountry, cityName, dateMap, DateUnit.DAY, Query.WORLD_DAILY_WEATHER);

                List<WorldDailyWeather> worldDailyWeatherList = weatherRootQuery.getWorldDailyWeather();

                if (Optional.ofNullable(foundCountry.getUid()).isPresent()) {
                    WorldDailyWeather newWorldDailyWeather = new WorldDailyWeather();

                    if (Optional.ofNullable(worldDailyWeatherList).isPresent() && !worldDailyWeatherList.isEmpty()) {
                        WorldDailyWeather oldWorldDailyWeather = worldDailyWeatherList.get(0);
                        newWorldDailyWeather.setWorldDailyWeather(oldWorldDailyWeather.getUid(), day);
                        weatherDao.updateWeatherNode(newWorldDailyWeather);
                    } else {
                        String worldDailyWeatherUid = null;
                        if (Optional.ofNullable(foundCountry.getWorldDailyWeathers()).isPresent() && !foundCountry.getWorldDailyWeathers().isEmpty()) {
                            worldDailyWeatherUid = foundCountry.getWorldDailyWeathers().get(0).getUid();
                        }
                        newWorldDailyWeather.setUid(worldDailyWeatherUid);

                        newWorldDailyWeather = new WorldDailyWeather();
                        newWorldDailyWeather.setWorldDailyWeather(worldDailyWeatherUid, day);

                        foundCountry.getWorldDailyWeathers().add(newWorldDailyWeather);

                        regionDao.updateRegionNode(foundCountry);

                        AtomicReference<Optional<WorldDailyWeather>> foundWorldDailyWeather = new AtomicReference<>(Optional.empty());
                        Optional.ofNullable(foundCountry.getUid()).ifPresent(uid -> foundWorldDailyWeather.set(weatherDao.getWorldDailyWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, day.getTm().replaceAll("/", ""))));

                        Optional<Day> dayNode = dateDao.getDayNode(dateMap);

                        if (foundWorldDailyWeather.get().isPresent()) {
                            dayNode.ifPresent(notNullDay -> notNullDay.getWorldDailyWeathers().add(foundWorldDailyWeather.get().get()));
                            dateDao.updateDateNode(dayNode);
                        }
                        dayNode.ifPresent(notNullDay -> log.info("[Service] callKweatherDay7Api - " + notNullDay.getDay() + "일의 " + "지역->날씨<-날짜 노드 연결 완료"));
                    }
                }
            }
            log.info("[Service] callKweatherDay7Api - 나라 : " + area.getDay0().getCity());
        }
    }

    /*public void callWorldWeatherApiOfKweather(KweatherWorldTopModel kweatherWorldTopModel, URI kweatherWorldUri, Map<DateUnit, Integer> dateMap) {
        try {
            kweatherWorldTopModel = restTemplate.getForObject(kweatherWorldUri, KweatherWorldTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiCallException();
        }

        for (AreaOfWorld area : kweatherWorldTopModel.getAreas()) {
            WeatherRootQuery weatherRootQuery = new WeatherRootQuery();
            Regions foundCountry = new Regions();

            String cityName = area.getDay0().getCity();
            checkAlreadyExistingWeatherNodeOfWorld(weatherRootQuery, foundCountry, cityName, dateMap, DateUnit.DAY, Query.WORLD_DAILY_WEATHER);

            List<WorldDailyWeather> worldDailyWeatherList = weatherRootQuery.getWorldDailyWeather();

            if(Optional.ofNullable(foundCountry.getUid()).isPresent()) {
                WorldDailyWeather newWorldDailyWeather = new WorldDailyWeather();

                if (Optional.ofNullable(worldDailyWeatherList).isPresent() && !worldDailyWeatherList.isEmpty()) {
                    for (DayOfWorld day : area.getDayList()) {
                        WorldDailyWeather oldWorldDailyWeather = worldDailyWeatherList.get(0);
                        newWorldDailyWeather.setWorldDailyWeather(oldWorldDailyWeather.getUid(), day);
                        weatherDao.updateWeatherNode(newWorldDailyWeather);
                    }
                } else {
                    for (DayOfWorld oneDay : area.getDayList()) {
                        AtomicReference<Optional<WorldDailyWeather>> foundWorldDailyWeather = new AtomicReference<>(Optional.empty());
                        Optional.ofNullable(foundCountry.getUid()).ifPresent(uid -> foundWorldDailyWeather.set(weatherDao.getWorldDailyWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, oneDay.getTm().replaceAll("/", ""))));

                        AtomicReference<String> worldDailyWeatherUid = new AtomicReference<>();
                        foundWorldDailyWeather.get().ifPresent(notNullWorldDailyWeather-> worldDailyWeatherUid.set(notNullWorldDailyWeather.getUid()));

                        newWorldDailyWeather = new WorldDailyWeather();
                        newWorldDailyWeather.setWorldDailyWeather(worldDailyWeatherUid.get(), oneDay);

                        foundCountry.getWorldDailyWeathers().add(newWorldDailyWeather);
                    }

                    regionDao.updateRegionNode(foundCountry);

                    for (DayOfWorld oneDay : area.getDayList()) {
                        AtomicReference<Optional<WorldDailyWeather>> foundWorldDailyWeather = new AtomicReference<>(Optional.empty());
                        Optional.ofNullable(foundCountry.getUid()).ifPresent(uid -> foundWorldDailyWeather.set(weatherDao.getWorldDailyWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, oneDay.getTm().replaceAll("/", ""))));

                        Map<DateUnit, Integer> forecastDateMap = dateService.splitDateIncludingDelim(oneDay.getTm());
                        Optional<Day> dayNode = dateDao.getDayNode(forecastDateMap);

                        if(foundWorldDailyWeather.get().isPresent()) {
                            dayNode.ifPresent(day -> day.getWorldDailyWeathers().add(foundWorldDailyWeather.get().get()));
                            dateDao.updateDateNode(dayNode);
                        }
                        dayNode.ifPresent(day -> log.info("[Service] callKweatherDay7Api - " + day.getDay() + "일의 " + "지역->날씨<-날짜 노드 연결 완료"));
                    }
                }
            }
            log.info("[Service] callKweatherDay7Api - 나라 : " + area.getDay0().getCity());
        }
    }*/

    private void checkAlreadyExistingWeatherNodeOfWorld(WeatherRootQuery weatherRootQuery, Regions foundCountry, String cityName, Map<DateUnit, Integer> dateMap, DateUnit dateUnit, Query query) {
        weatherRootQuery.setWeatherRootQuery(weatherDao.getAlreadyExistingWeatherNodeOfWorldWithRegionNameAndDate(cityName, dateMap, dateUnit, query));
        if(Optional.ofNullable(weatherRootQuery.getRegion()).isPresent() && !weatherRootQuery.getRegion().isEmpty()) {
            foundCountry.setCountry(weatherRootQuery.getRegion().get(0));
        }
    }

    public void callSpecialWeatherApi() {
        SpecialWeatherInfoTopModel specialWeatherInfoTopModel = new SpecialWeatherInfoTopModel();

       String todayDate = dateService.makeCurrentDateFormat();
        URI uri = URI.create(specialWeatherApiUrl + "?ServiceKey=" + serviceKey + "&fromTmFc=" + todayDate + "&toTmFc=" + todayDate + "&_type=json");

        try {
            specialWeatherInfoTopModel = restTemplate.getForObject(uri, SpecialWeatherInfoTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiCallException();
        }

        Optional<List<SpecialWeatherInfo>> optionalSpecialWeatherInfoList = Optional.ofNullable(specialWeatherInfoTopModel)
                .map(SpecialWeatherInfoTopModel::getResponse)
                .map(SpecialWeatherInfoResponse::getBody)
                .map(SpecialWeatherInfoBody::getItems)
                .map(SpecialWeatherInfoItems::getItem);

        Optional<Regions> optionalKorea = regionDao.getCountryNodeWithName(CountryList.KOREA.getCountryName());
        if(optionalKorea.isPresent()) {
            String countryUid = optionalKorea.get().getUid();

            if (optionalSpecialWeatherInfoList.isPresent() && !optionalSpecialWeatherInfoList.get().isEmpty()) {
                SpecialWeatherInfo specialWeatherInfo = optionalSpecialWeatherInfoList.get().get(0);

                String date = specialWeatherInfo.getTmFc().substring(0, 8);
                String time = specialWeatherInfo.getTmFc().substring(8, 12);
                Map<DateUnit, Integer> dateMap = dateService.splitDateAndTime(date, time);

                WeatherRootQuery weatherRootQuery = new WeatherRootQuery();
                Regions foundCountry = new Regions();
                checkAlreadyExistingSpecialWeatherNode(weatherRootQuery, foundCountry, countryUid, dateMap, DateUnit.HOUR, Query.SPECIAL_WEATHER);

                List<SpecialWeather> specialWeatherList = weatherRootQuery.getSpecialWeather();

                SpecialWeather newSpecialWeather = new SpecialWeather();

                if(Optional.ofNullable(foundCountry.getUid()).isPresent()) {
                    if (Optional.ofNullable(specialWeatherList).isPresent() && !specialWeatherList.isEmpty()) {
                        SpecialWeather oldSpecialWeather = specialWeatherList.get(0);
                        newSpecialWeather.setSpecialWeather(oldSpecialWeather.getUid(), specialWeatherInfo);
                        weatherDao.updateWeatherNode(newSpecialWeather);
                    } else {
                        AtomicReference<Optional<SpecialWeather>> foundSpecialWeather1 = new AtomicReference<>(Optional.empty());
                        Optional.ofNullable(foundCountry.getUid()).ifPresent(uid -> foundSpecialWeather1.set(weatherDao.getSpecialWeatherNodeLinkedToRegionWithRegionUidAndDate(uid, date, time)));

                        AtomicReference<String> specialWeatherUid = new AtomicReference<>();
                        foundSpecialWeather1.get().ifPresent(notNullSpecialWeather -> specialWeatherUid.set(notNullSpecialWeather.getUid()));

                        newSpecialWeather = new SpecialWeather();
                        newSpecialWeather.setSpecialWeather(specialWeatherUid.get(), specialWeatherInfo);

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
        if(Optional.ofNullable(weatherRootQuery.getRegion()).isPresent() && !weatherRootQuery.getRegion().isEmpty()) {
            foundCountry.setCountry(weatherRootQuery.getRegion().get(0));
        }
    }
}
