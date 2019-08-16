package com.example.realpilot.service;

import com.example.realpilot.dao.DateDao;
import com.example.realpilot.dao.DisasterDao;
import com.example.realpilot.dao.RegionDao;
import com.example.realpilot.exceptionList.ApiCallException;
import com.example.realpilot.externalApiModel.earthquake.*;
import com.example.realpilot.model.date.Hour;
import com.example.realpilot.model.disaster.DisasterRootQuery;
import com.example.realpilot.model.disaster.Earthquake;
import com.example.realpilot.model.region.Overseas;
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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class DisasterService {
    private static final Logger log = LoggerFactory.getLogger(DisasterService.class);

    @Autowired
    private DateService dateService;
    @Autowired
    private RegionDao regionDao;
    @Autowired
    private DateDao dateDao;
    @Autowired
    private DisasterDao disasterDao;


    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.serviceKey}")
    private String serviceKey;
    @Value("${earthquake.api.url}")
    private String earthquakeApiUrl;

    public void callEarthquakeApi() {
        String todayDate = dateService.makeCurrentDateFormat();
        String fewDaysAgoDate = dateService.makeDateOfFewDaysAgoFormat(3);

        EarthquakeInfoTopModel earthquakeInfoTopModel = new EarthquakeInfoTopModel();

        URI uri = URI.create(earthquakeApiUrl + "?ServiceKey=" + serviceKey + "&fromTmFc=" + fewDaysAgoDate + "&toTmFc=" + todayDate + "&_type=json");

        try {
            earthquakeInfoTopModel = restTemplate.getForObject(uri, EarthquakeInfoTopModel.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiCallException();
        }
        // TODO: 데이터 안올 때 값 확인하기
        Optional<List<EarthquakeInfo>> optionalEarthquakeInfoList = Optional.ofNullable(earthquakeInfoTopModel)
                .map(EarthquakeInfoTopModel::getResponse)
                .map(EarthquakeInfoResponse::getBody)
                .map(EarthquakeInfoBody::getItems)
                .map(EarthquakeInfoItems::getItem);

        if(optionalEarthquakeInfoList.isPresent() && !optionalEarthquakeInfoList.get().isEmpty()) {
            List<EarthquakeInfo> earthquakeInfoList = optionalEarthquakeInfoList.get();

            for(EarthquakeInfo earthquakeInfo : earthquakeInfoList) {
                String uid = null;
                int forecastType =  earthquakeInfo.getFcTp();
                switch (forecastType) {
                    case 2:
                    case 12:
                        Optional<Overseas> optionalOverseas = regionDao.getOverseasNode();
                        if(optionalOverseas.isPresent()) {
                            uid = optionalOverseas.get().getUid();
                        }
                        break;
                    case 3:
                    case 5:
                    case 11:
                        Optional<Regions> optionalKorea = regionDao.getCountryNodeWithName(CountryList.KOREA.getCountryName());
                        if(optionalKorea.isPresent()) {
                            uid = optionalKorea.get().getUid();
                        }
                        break;
                }

                String occurrenceDateAndTime = earthquakeInfo.getTmEqk();
                String date = occurrenceDateAndTime.substring(0, 8);
                String time = occurrenceDateAndTime.substring(8, 14);
                Map<DateUnit, Integer> dateMap = dateService.splitDateAndTime(date, time);

                DisasterRootQuery disasterRootQuery = new DisasterRootQuery();
                Regions foundCountry = new Regions();
                checkAlreadyExistingEarthquakeNode(disasterRootQuery, foundCountry, uid, dateMap, DateUnit.HOUR, Query.EARTHQUAKE);

                List<Earthquake> earthquakeList = disasterRootQuery.getEarthquake();

                Earthquake newEarthquake = new Earthquake();

                if(Optional.ofNullable(foundCountry.getUid()).isPresent()) {
                    if(Optional.of(earthquakeList).isPresent() && !earthquakeList.isEmpty()) {
                        Earthquake oldEarthquake = earthquakeList.get(0);
                        newEarthquake.setEarthquake(oldEarthquake.getUid(), earthquakeInfo);
                        disasterDao.updateDisasterNode(newEarthquake);
                    } else {
                        String earthquakeUid = null;
                        if (Optional.ofNullable(foundCountry.getEarthquakes()).isPresent() && !foundCountry.getEarthquakes().isEmpty()) {
                            earthquakeUid = foundCountry.getEarthquakes().get(0).getUid();
                        }
                        newEarthquake.setUid(earthquakeUid);

                        /*AtomicReference<Optional<Earthquake>> foundEarthquake1 = new AtomicReference<>(Optional.empty());
                        Optional.ofNullable(uid).ifPresent(notNullUid -> foundEarthquake1.set(disasterDao.getEarthquakeNodeLinkedToRegionWithRegionUidAndDate(notNullUid, date, time)));

                        AtomicReference<String> earthquakeUid = new AtomicReference<>();
                        foundEarthquake1.get().ifPresent(notNullEarthquake -> earthquakeUid.set(notNullEarthquake.getUid()));*/

                        newEarthquake = new Earthquake();
                        newEarthquake.setEarthquake(earthquakeUid, earthquakeInfo);

                        foundCountry.getEarthquakes().add(newEarthquake);
                        regionDao.updateRegionNode(foundCountry);

                        AtomicReference<Optional<Earthquake>> foundEarthquake = new AtomicReference<>(Optional.empty());
                        Optional.ofNullable(foundCountry.getUid()).ifPresent(notNullUid -> foundEarthquake.set(disasterDao.getEarthquakeNodeLinkedToRegionWithRegionUidAndDate(notNullUid, date, time)));

                        Optional<Hour> hourNode = dateDao.getHourNode(dateMap);

                        if (foundEarthquake.get().isPresent()) {
                            hourNode.ifPresent(hour -> hour.getEarthquakes().add(foundEarthquake.get().get()));
                        }
                        dateDao.updateDateNode(hourNode);

                        foundCountry.getEarthquakes().clear();
                    }
                }
            }
        }
    }

    private void checkAlreadyExistingEarthquakeNode(DisasterRootQuery disasterRootQuery, Regions foundCountry, String uid, Map<DateUnit, Integer> dateMap, DateUnit dateUnit, Query query) {
        disasterRootQuery.setDisasterRootQuery(disasterDao.getAlreadyExistingDisasterNodeWithRegionUidAndDate(uid, dateMap, dateUnit, query));
        if(Optional.ofNullable(disasterRootQuery.getRegion()).isPresent() && !disasterRootQuery.getRegion().isEmpty()) {
            foundCountry.setCountry(disasterRootQuery.getRegion().get(0));
        }
    }
}
