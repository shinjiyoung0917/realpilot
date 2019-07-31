package com.example.realpilot.service;

import com.example.realpilot.dao.AirPollutionDao;
import com.example.realpilot.dao.RegionDao;
import com.example.realpilot.externalApiModel.nearbyMeasureStationList.NearbyMeasureStationList;
import com.example.realpilot.externalApiModel.nearbyMeasureStationList.NearbyMeasureStationListTopModel;
import com.example.realpilot.externalApiModel.realTimeAirPollutionInfo.RealTimeAirPollutionInfoTopModel;
import com.example.realpilot.model.airPollution.AirPollutionDetail;
import com.example.realpilot.model.airPollution.AirPollutionRootQuery;
import com.example.realpilot.model.region.Eubmyeondong;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class AirPollutionService {
    private static final Logger log = LoggerFactory.getLogger(AirPollutionService.class);

    @Autowired
    private DateService dateService;
    @Autowired
    private RegionDao regionDao;
    @Autowired
    private AirPollutionDao airPollutionDao;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.serviceKey}")
    private String serviceKey;
    @Value("${nearbyMeasureStationList.api.url}")
    private String measureStationApiUrl;
    @Value("${realtimeAirPollutionInfo.api.url}")
    private String realTimeMeasureInfoApiUrl;

    public void callNearbyMeasureStationListApi() {
        NearbyMeasureStationListTopModel nearbyMeasureStationListTopModel = new NearbyMeasureStationListTopModel();

        Optional<Set<Eubmyeondong>> optionalTmCoordList = regionDao.getTmCoordinateList();

        if(optionalTmCoordList.isPresent()) {
            for (Eubmyeondong eubmyeongdong : optionalTmCoordList.get()) {
                URI uri = URI.create(measureStationApiUrl + "?ServiceKey=" + serviceKey + "&tmX=" + eubmyeongdong.getTmX() + "&tmY=" + eubmyeongdong.getTmY() + "&_returnType=json");

                try {
                    nearbyMeasureStationListTopModel = restTemplate.getForObject(uri, NearbyMeasureStationListTopModel.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                List<NearbyMeasureStationList> measureStationList = nearbyMeasureStationListTopModel.getList();
                if(Optional.ofNullable(measureStationList).isPresent() && !measureStationList.isEmpty()) {
                    eubmyeongdong.setEubmyeondongByMeasureSation(measureStationList.get(0));
                    regionDao.updateRegionNode(eubmyeongdong);
                }
            }
        }
    }

    public void callRealTimeAirPollutionInfoApi() {
        RealTimeAirPollutionInfoTopModel realTimeAirPollutionInfoTopModel = new RealTimeAirPollutionInfoTopModel();

        Optional<Set<Eubmyeondong>> optionalMeasureStationList = regionDao.getMeasureStationList();

        if(optionalMeasureStationList.isPresent()) {
            for (Eubmyeondong eubmyeongdong : optionalMeasureStationList.get()) {
                AirPollutionRootQuery airPollutionRootQuery = new AirPollutionRootQuery();
                checkAlreadyExistingAirPollutionDeatailNode(airPollutionRootQuery, eubmyeongdong, eubmyeongdong.getMeasureStationName(), eubmyeongdong.getMeasureStationAddr(), DateUnit.HOUR, Query.AIR_POLLUTION_DETAIL);

                List<AirPollutionDetail> airPollutionDetailList = airPollutionRootQuery.getAirPollutionDetail();

                
                /*AirPollutionDetail airPollutionDetail = new AirPollutionDetail();
                eubmyeongdong.setRegion();
                */
            }
        }
    }

    private void checkAlreadyExistingAirPollutionDeatailNode(AirPollutionRootQuery airPollutionRootQuery, Regions foundRegion, String measureStationName, String meausureStationAddr, DateUnit dateUnit, Query query) {
        Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();
        airPollutionRootQuery.setAirPollutionRootQuery(airPollutionDao.getAlreadyExistingAirPollutionDeatailNodeWithMeasureStationInfoAndDate(measureStationName, meausureStationAddr, dateMap, dateUnit, query));
        Optional.ofNullable(airPollutionRootQuery.getRegion()).ifPresent(region -> foundRegion.setRegion(region.get(0)));
    }
}
