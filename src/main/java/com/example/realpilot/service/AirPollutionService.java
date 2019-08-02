package com.example.realpilot.service;

import com.example.realpilot.dao.AirPollutionDao;
import com.example.realpilot.dao.DateDao;
import com.example.realpilot.dao.RegionDao;
import com.example.realpilot.externalApiModel.nearbyMeasureStationList.NearbyMeasureStationList;
import com.example.realpilot.externalApiModel.nearbyMeasureStationList.NearbyMeasureStationListTopModel;
import com.example.realpilot.externalApiModel.realTimeAirPollutionInfo.RealTimeAirPollutionInfo;
import com.example.realpilot.externalApiModel.realTimeAirPollutionInfo.RealTimeAirPollutionInfoTopModel;
import com.example.realpilot.model.airPollution.AirPollutionDetail;
import com.example.realpilot.model.airPollution.AirPollutionRootQuery;
import com.example.realpilot.model.date.Hour;
import com.example.realpilot.model.region.Regions;
import com.example.realpilot.utilAndConfig.DateUnit;
import com.example.realpilot.utilAndConfig.Query;
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
            for (Regions eubmyeongdong : optionalMeasureStationList.get()) {
                URI uri = URI.create(realTimeAirPollutionInfoApiUrl + "?ServiceKey=" + serviceKey + "&stationName=" + eubmyeongdong.getMeasureStationName() + "&dataTerm=daily&ver=1.3&_returnType=json");

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

                List<RealTimeAirPollutionInfo> realTimeAirPollutionInfoListList = realTimeAirPollutionInfoTopModel.getList();

                if(Optional.ofNullable(realTimeAirPollutionInfoListList).isPresent() && !realTimeAirPollutionInfoListList.isEmpty()) {
                    List<Regions> foundEubmyeondongList = new ArrayList<>();
                    AirPollutionRootQuery airPollutionRootQuery = new AirPollutionRootQuery();
                    // TODO: 처음부터 지역->날씨<-날짜 노드 한 번에 가져오지 말고, 측정소로만 조회해서 지역노드들 가져오고 그 지역노드들의 uid로 지역->날씨<-날짜 노드 조회하도록 수정
                    checkAlreadyExistingAirPollutionDeatailNode(airPollutionRootQuery, foundEubmyeondongList, eubmyeongdong.getMeasureStationName(), DateUnit.HOUR, Query.AIR_POLLUTION_DETAIL);

                    List<AirPollutionDetail> airPollutionDetailList = airPollutionRootQuery.getAirPollutionDetail();

                    String dateAndTime = realTimeAirPollutionInfoListList.get(0).getDataTime().replaceAll("-|:", "");
                    String date = dateAndTime.substring(0, 8);
                    String time = dateAndTime.substring(9, 13);

                    int index = 0;
                    for(Regions foundEubmyeondong : foundEubmyeondongList) {
                        if (Optional.ofNullable(foundEubmyeondong.getUid()).isPresent()) {
                            AirPollutionDetail newAirPollutionDetail = new AirPollutionDetail();

                            if (Optional.ofNullable(airPollutionDetailList).isPresent() && !airPollutionDetailList.isEmpty()) {
                                AirPollutionDetail oldAirPollutionDetail = airPollutionDetailList.get(index++);
                                newAirPollutionDetail.setAirPollutionDetail(oldAirPollutionDetail.getUid(), realTimeAirPollutionInfoListList.get(0));
                                airPollutionDao.updateAirPollutionNode(newAirPollutionDetail);
                            } else {
                                AtomicReference<Optional<AirPollutionDetail>> foundAirPollutionDetail1 = new AtomicReference<>();
                                Optional.ofNullable(foundEubmyeondong.getUid()).ifPresent(uid -> foundAirPollutionDetail1.set(airPollutionDao.getAirPollutionDetailNodeLinkedToRegionWithRegionUidAndDate(uid, date, time)));

                                AtomicReference<String> airPollutionDetailUid = new AtomicReference<>(null);
                                foundAirPollutionDetail1.get().ifPresent(notNullAirPollutionDetail-> airPollutionDetailUid.set(notNullAirPollutionDetail.getUid()));

                                newAirPollutionDetail = new AirPollutionDetail();
                                newAirPollutionDetail.setAirPollutionDetail(airPollutionDetailUid.get(), realTimeAirPollutionInfoListList.get(0));

                                foundEubmyeondong.getAirPollutionDetails().add(newAirPollutionDetail);

                                regionDao.updateRegionNode(foundEubmyeondong);

                                AtomicReference<Optional<AirPollutionDetail>> foundAirPollutionDetail2 = new AtomicReference<>();
                                Optional.ofNullable(foundEubmyeondong.getUid()).ifPresent(uid -> foundAirPollutionDetail2.set(airPollutionDao.getAirPollutionDetailNodeLinkedToRegionWithRegionUidAndDate(uid, date, time)));

                                Map<DateUnit, Integer> dateMap = dateService.getFcstDate(date, time);
                                Optional<Hour> hourNode = dateDao.getHourNode(dateMap);

                                hourNode.ifPresent(hour -> hour.getAirPollutionDetails().add(foundAirPollutionDetail2.get().get()));
                                dateDao.updateDateNode(hourNode);
                            }
                            log.info("[Service] callRealTimeAirPollutionInfoApi - " + time + "시의 " + "지역->대기질<-날짜 노드 연결 완료");
                        }
                    }
                }
            }
        }
    }

    private void checkAlreadyExistingAirPollutionDeatailNode(AirPollutionRootQuery airPollutionRootQuery, List<Regions> foundRegion, String measureStationName, DateUnit dateUnit, Query query) {
        Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();
        airPollutionRootQuery.setAirPollutionRootQuery(airPollutionDao.getAlreadyExistingAirPollutionDeatailNodeWithMeasureStationInfoAndDate(measureStationName, dateMap, dateUnit, query));
        Optional.ofNullable(airPollutionRootQuery.getRegion()).ifPresent(region -> foundRegion.addAll(region));
    }
}
