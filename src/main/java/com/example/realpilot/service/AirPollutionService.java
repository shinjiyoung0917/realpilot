package com.example.realpilot.service;

import com.example.realpilot.dao.AirPollutionDao;
import com.example.realpilot.dao.DateDao;
import com.example.realpilot.dao.RegionDao;
import com.example.realpilot.externalApiModel.airPollutionForecastOverall.AirPollutionForecastOverall;
import com.example.realpilot.externalApiModel.airPollutionForecastOverall.AirPollutionForecastOverallTopModel;
import com.example.realpilot.externalApiModel.nearbyMeasureStationList.NearbyMeasureStationList;
import com.example.realpilot.externalApiModel.nearbyMeasureStationList.NearbyMeasureStationListTopModel;
import com.example.realpilot.externalApiModel.realTimeAirPollutionInfo.RealTimeAirPollutionInfo;
import com.example.realpilot.externalApiModel.realTimeAirPollutionInfo.RealTimeAirPollutionInfoTopModel;
import com.example.realpilot.model.airPollution.AirPollutionDetail;
import com.example.realpilot.model.airPollution.AirPollutionOverall;
import com.example.realpilot.model.airPollution.AirPollutionRootQuery;
import com.example.realpilot.model.date.Day;
import com.example.realpilot.model.date.Hour;
import com.example.realpilot.model.region.Regions;
import com.example.realpilot.utilAndConfig.CountryList;
import com.example.realpilot.utilAndConfig.DateUnit;
import com.example.realpilot.utilAndConfig.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.swing.text.html.Option;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AirPollutionService {
    private static final Logger log = LoggerFactory.getLogger(AirPollutionService.class);

    @Autowired
    private DateService dateService;
    @Autowired
    private RegionDao regionDao;
    @Autowired
    private DateDao dateDao;
    @Autowired
    private AirPollutionDao airPollutionDao;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.serviceKey}")
    private String serviceKey;
    @Value("${nearbyMeasureStationList.api.url}")
    private String measureStationApiUrl;
    @Value("${realTimeAirPollutionInfo.api.url}")
    private String realTimeAirPollutionInfoApiUrl;
    @Value("${airPollutionForecast.api.url}")
    private String airPollutionForecastApiUrl;

    public void callNearbyMeasureStationListApi() {
        NearbyMeasureStationListTopModel nearbyMeasureStationListTopModel = new NearbyMeasureStationListTopModel();

        Optional<Set<Regions>> optionalTmCoordList = regionDao.getTmCoordinateList();

        // TODO: optional isPresent 걸어주면 비동기처럼 수행되는 느낌..?
        //if(optionalTmCoordList.isPresent()) {
            for (Regions eubmyeongdong : optionalTmCoordList.get()) {
                URI uri = URI.create(measureStationApiUrl + "?ServiceKey=" + serviceKey + "&tmX=" + eubmyeongdong.getTmX() + "&tmY=" + eubmyeongdong.getTmY() + "&_returnType=json");

                try {
                    nearbyMeasureStationListTopModel = restTemplate.getForObject(uri, NearbyMeasureStationListTopModel.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // TODO: Read timed out 발생하면 재실행되도록?
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                List<NearbyMeasureStationList> measureStationList = nearbyMeasureStationListTopModel.getList();
                if(Optional.ofNullable(measureStationList).isPresent() && !measureStationList.isEmpty()) {
                    eubmyeongdong.setEubmyeondongByMeasureSation(measureStationList.get(0));
                    regionDao.updateRegionNode(eubmyeongdong);
                }
            }
        //}
    }

    public void callRealTimeAirPollutionInfoApi() {
        RealTimeAirPollutionInfoTopModel realTimeAirPollutionInfoTopModel = new RealTimeAirPollutionInfoTopModel();

        Optional<Set<Regions>> optionalMeasureStationList = regionDao.getMeasureStationList();

        if(optionalMeasureStationList.isPresent()) {
            for (Regions stationOfUmd : optionalMeasureStationList.get()) {
                URI uri = URI.create(realTimeAirPollutionInfoApiUrl + "?ServiceKey=" + serviceKey + "&stationName=" + stationOfUmd.getMeasureStationName() + "&dataTerm=daily&ver=1.3&_returnType=json");

                try {
                    realTimeAirPollutionInfoTopModel = restTemplate.getForObject(uri, RealTimeAirPollutionInfoTopModel.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // TODO: Read timed out 발생하면 재실행되도록?
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                List<RealTimeAirPollutionInfo> realTimeAirPollutionInfoList = realTimeAirPollutionInfoTopModel.getList();

                if(Optional.ofNullable(realTimeAirPollutionInfoList).isPresent() && !realTimeAirPollutionInfoList.isEmpty()) {
                    Regions foundEubmyeondong = new Regions();
                    AirPollutionRootQuery airPollutionRootQuery = new AirPollutionRootQuery();

                    Optional<List<Regions>> regionList = regionDao.getRegionNodeWithMeasureStation(stationOfUmd.getMeasureStationName());

                    if(regionList.isPresent()) {
                        for (Regions eubmyeondong : regionList.get()) {
                            if (Optional.ofNullable(eubmyeondong.getUid()).isPresent()) {
                                String dateAndTime = realTimeAirPollutionInfoList.get(0).getDataTime().replaceAll("-|:", "");
                                String date = dateAndTime.substring(0, 8);
                                String time = dateAndTime.substring(9, 13);

                                checkAlreadyExistingAirPollutionDetailNode(airPollutionRootQuery, foundEubmyeondong, eubmyeondong.getUid(), DateUnit.HOUR, Query.AIR_POLLUTION_DETAIL);

                                List<AirPollutionDetail> airPollutionDetailList = airPollutionRootQuery.getAirPollutionDetail();

                                AirPollutionDetail newAirPollutionDetail = new AirPollutionDetail();

                                if (Optional.ofNullable(airPollutionDetailList).isPresent() && !airPollutionDetailList.isEmpty()) {
                                    AirPollutionDetail oldAirPollutionDetail = airPollutionDetailList.get(0);
                                    newAirPollutionDetail.setAirPollutionDetail(oldAirPollutionDetail.getUid(), realTimeAirPollutionInfoList.get(0));
                                    airPollutionDao.updateAirPollutionNode(newAirPollutionDetail);
                                } else {
                                    AtomicReference<Optional<AirPollutionDetail>> foundAirPollutionDetail1 = new AtomicReference<>(Optional.empty());
                                    Optional.ofNullable(eubmyeondong.getUid()).ifPresent(uid -> foundAirPollutionDetail1.set(airPollutionDao.getAirPollutionDetailNodeLinkedToRegionWithRegionUidAndDate(uid, date, time)));

                                    AtomicReference<String> airPollutionDetailUid = new AtomicReference<>();
                                    foundAirPollutionDetail1.get().ifPresent(notNullAirPollutionDetail -> airPollutionDetailUid.set(notNullAirPollutionDetail.getUid()));

                                    newAirPollutionDetail = new AirPollutionDetail();
                                    newAirPollutionDetail.setAirPollutionDetail(airPollutionDetailUid.get(), realTimeAirPollutionInfoList.get(0));

                                    foundEubmyeondong.getAirPollutionDetails().add(newAirPollutionDetail);
                                    regionDao.updateRegionNode(foundEubmyeondong);

                                    AtomicReference<Optional<AirPollutionDetail>> foundAirPollutionDetail2 = new AtomicReference<>(Optional.empty());
                                    Optional.ofNullable(foundEubmyeondong.getUid()).ifPresent(uid -> foundAirPollutionDetail2.set(airPollutionDao.getAirPollutionDetailNodeLinkedToRegionWithRegionUidAndDate(uid, date, time)));

                                    Map<DateUnit, Integer> dateMap = dateService.splitDateAndTime(date, time);
                                    Optional<Hour> hourNode = dateDao.getHourNode(dateMap);

                                    if (foundAirPollutionDetail2.get().isPresent()) {
                                        hourNode.ifPresent(hour -> hour.getAirPollutionDetails().add(foundAirPollutionDetail2.get().get()));
                                        dateDao.updateDateNode(hourNode);
                                    }
                                }
                                log.info("[Service] callRealTimeAirPollutionInfoApi - " + time + "시의 " + "지역->대기질<-날짜 노드 연결 완료");
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkAlreadyExistingAirPollutionDetailNode(AirPollutionRootQuery airPollutionRootQuery, Regions foundEubmyeondong, String uid, DateUnit dateUnit, Query query) {
        Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();
        airPollutionRootQuery.setAirPollutionRootQuery(airPollutionDao.getAlreadyExistingAirPollutionNodeWithRegionUidAndDate(uid, dateMap, "", dateUnit, query));
        Optional.ofNullable(airPollutionRootQuery.getRegion()).ifPresent(region -> foundEubmyeondong.setRegion(region.get(0)));
    }

    public void callAirPollutionForecastApi() {
        AirPollutionForecastOverallTopModel airPollutionForecastOverallTopModel = new AirPollutionForecastOverallTopModel();

        String searchDate = dateService.makeCurrentDateFormatWithBar();
        URI uri = URI.create(airPollutionForecastApiUrl + "?ServiceKey=" + serviceKey + "&searchDate=" + searchDate + "&_returnType=json");

        try {
            airPollutionForecastOverallTopModel = restTemplate.getForObject(uri, AirPollutionForecastOverallTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<AirPollutionForecastOverall> airPollutionForecastOverallList = airPollutionForecastOverallTopModel.getList();

        if(Optional.ofNullable(airPollutionForecastOverallList).isPresent() && !airPollutionForecastOverallList.isEmpty()) {
            String[] baseDateArray = airPollutionForecastOverallList.get(0).getDataTime().split(" ", 2);
            Map<DateUnit, Integer> dateMap = dateService.splitDateAndTime(baseDateArray[0].replaceAll("-", ""), baseDateArray[1]);
            parseAirPollutionForecastOverall(airPollutionForecastOverallList, dateMap);
        }
    }

    private void parseAirPollutionForecastOverall(List<AirPollutionForecastOverall> airPollutionForecastOverallList, Map<DateUnit, Integer> todayDateMap) {
        String prevDataTime = "";
        int index = 0;

        AirPollutionRootQuery airPollutionRootQuery = new AirPollutionRootQuery();
        Regions foundCountry = new Regions();

        Optional<Regions> optionalKorea = regionDao.getCountryNodeWithName(CountryList.KOREA.getCountryName());
        if(optionalKorea.isPresent()) {
            String countryUid = optionalKorea.get().getUid();

            for (AirPollutionForecastOverall airPollutionForecastOverall : airPollutionForecastOverallList) {
                String[] forecastDate = airPollutionForecastOverall.getInformData().split("-", 3);
                int forecastDay = Integer.parseInt(forecastDate[forecastDate.length - 1]);
                int dayTerm = forecastDay - todayDateMap.get(DateUnit.DAY);

                if (!prevDataTime.equals(airPollutionForecastOverall.getDataTime()) && index != 0) {
                    break;
                } else {
                    Map<DateUnit, Integer> forecastDateMap = dateService.splitDateIncludingDelim(airPollutionForecastOverall.getInformData());
                    if (dayTerm == 0) {
                        checkAlreadyExistingAirPollutionOverallNode(airPollutionRootQuery, foundCountry, countryUid, todayDateMap, airPollutionForecastOverall.getInformCode(), DateUnit.HOUR, Query.AIR_POLLUTION_OVERALL);
                    } else if (dayTerm >= 1) {
                        checkAlreadyExistingAirPollutionOverallNode(airPollutionRootQuery, foundCountry, countryUid, forecastDateMap, airPollutionForecastOverall.getInformCode(), DateUnit.DAY, Query.AIR_POLLUTION_OVERALL);
                    }

                    List<AirPollutionOverall> airPollutionOverallList = airPollutionRootQuery.getAirPollutionOverall();

                    AirPollutionOverall newAirPollutionOverall = new AirPollutionOverall();

                    if (Optional.ofNullable(airPollutionOverallList).isPresent() && !airPollutionOverallList.isEmpty()) {
                        AirPollutionOverall oldAirPollutionOverall = airPollutionOverallList.get(0);
                        newAirPollutionOverall.setAirPollutionOverall(oldAirPollutionOverall.getUid(), airPollutionForecastOverall, dayTerm);
                        airPollutionDao.updateAirPollutionNode(newAirPollutionOverall);
                    } else {
                        String date = airPollutionForecastOverall.getInformData().replaceAll("-", "");
                        String time = "";

                        if (dayTerm == 0) {
                            time = dateService.convertToDoubleDigit(todayDateMap.get(DateUnit.HOUR))  + "00";
                        }

                        AtomicReference<Optional<AirPollutionOverall>> foundAirPollutionOverall1 = new AtomicReference<>(Optional.empty());
                        String finalTime = time;
                        Optional.ofNullable(countryUid).ifPresent(uid -> foundAirPollutionOverall1.set(airPollutionDao.getAirPollutionOverallNodeLinkedToRegionWithRegionUidAndDate(countryUid, date, finalTime, airPollutionForecastOverall.getInformCode())));

                        AtomicReference<String> airPollutionOverallUid = new AtomicReference<>();
                        foundAirPollutionOverall1.get().ifPresent(notNullAirPollutionOverall -> airPollutionOverallUid.set(notNullAirPollutionOverall.getUid()));

                        newAirPollutionOverall = new AirPollutionOverall();
                        newAirPollutionOverall.setAirPollutionOverall(airPollutionOverallUid.get(), airPollutionForecastOverall, dayTerm);

                        foundCountry.getAirPollutionOveralls().add(newAirPollutionOverall);
                        regionDao.updateRegionNode(foundCountry);

                        AtomicReference<Optional<AirPollutionOverall>> foundAirPollutionOverall2 = new AtomicReference<>(Optional.empty());
                        Optional.ofNullable(foundCountry.getUid()).ifPresent(uid -> foundAirPollutionOverall2.set(airPollutionDao.getAirPollutionOverallNodeLinkedToRegionWithRegionUidAndDate(uid, date, finalTime, airPollutionForecastOverall.getInformCode())));

                        if (dayTerm == 0) {
                            Optional<Hour> hourNode = dateDao.getHourNode(todayDateMap);

                            if (foundAirPollutionOverall2.get().isPresent()) {
                                hourNode.ifPresent(hour -> hour.getAirPollutionOveralls().add(foundAirPollutionOverall2.get().get()));
                            }
                            dateDao.updateDateNode(hourNode);
                        } else if (dayTerm >= 1) {
                            Optional<Day> dayNode = dateDao.getDayNode(forecastDateMap);

                            if (foundAirPollutionOverall2.get().isPresent()) {
                                dayNode.ifPresent(day -> day.getAirPollutionOveralls().add(foundAirPollutionOverall2.get().get()));
                            }
                            dateDao.updateDateNode(dayNode);
                        }

                        foundCountry.getAirPollutionOveralls().clear();
                    }
                }
                prevDataTime = airPollutionForecastOverall.getDataTime();

                ++index;
                log.info("[Service] parseAirPollutionAmForecastOverall - " + prevDataTime + "의 " + "지역->대기질<-날짜 노드 연결 완료");
            }
        }
    }

    private void checkAlreadyExistingAirPollutionOverallNode(AirPollutionRootQuery airPollutionRootQuery, Regions foundCountry, String uid, Map<DateUnit, Integer> dateMap, String airPollutionCode, DateUnit dateUnit, Query query) {
        airPollutionRootQuery.setAirPollutionRootQuery(airPollutionDao.getAlreadyExistingAirPollutionNodeWithRegionUidAndDate(uid, dateMap, airPollutionCode, dateUnit, query));
        Optional.ofNullable(airPollutionRootQuery.getRegion()).ifPresent(country -> foundCountry.setCountry(country.get(0)));
    }

    public void callYellowDustInfoApi() {

    }
}
