package com.example.realpilot.service;

import com.example.realpilot.externalApiModel.tmCoordinate.OpenModel;
import com.example.realpilot.externalApiModel.tmCoordinate.TmCoordinate;
import com.example.realpilot.utilAndConfig.SidoList;
import com.example.realpilot.utilAndConfig.WxMappingJackson2HttpMessageConverter;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.*;
import java.util.*;

@Service
public class LoadTmCoordinateService {
    private static final Logger log = LoggerFactory.getLogger(LoadExcelFileService.class);

    @Autowired
    private LoadExcelFileService loadExcelFileService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    protected SessionFactory sessionFactory;

    @Autowired
    public LoadTmCoordinateService(SessionFactory sessionFactory) {
        //this.sessionFactory = sessionFactory;
    }

    @Value("${tmCoordinate.api.url}")
    private String tmCoordinateApiUrl;


    public void callTmCoordinateApi() {
        restTemplate.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());

        String serviceKey = "y7hP75dT%2FAxfrX5WKcSg31EZIT%2BUXZPOaBXR%2F2xX9oyO1gKR1v5opJc0oX1VjhMB4s45r38hJgfhNfTtYJNyBg%3D%3D";

        OpenModel openModel = null;
        for(SidoList sido : SidoList.values()) {
            log.info("callTmCoordinateApi - 시도 이름 : " + sido.getSidoName());

            URI uri = URI.create(tmCoordinateApiUrl + "?ServiceKey=" + serviceKey + "&umdName=" + sido.getSidoName() + "&numOfRows=" + 500 + "&_returnType=json");

            openModel = restTemplate.getForObject(uri, OpenModel.class);

            for (TmCoordinate tm : openModel.getList()) {
                log.info("callTmCoordinateApi - tm좌표 : " + tm.getSidoName() + tm.getSggName() + tm.getUmdName() + ", " + tm.getTmX() + ", " + tm.getTmY() + ", " + tm.getUmdName());
            }

            // TODO: API 한 번 호출하고 나서 http 연결 끊어주고 다시 연결해야하 하는지? connection reset 발생함
            //restTemplate.delete(testUrl);
        }
        parseTmCoordinate(openModel.getList());
    }

    private void parseTmCoordinate(List<TmCoordinate> tmCoordinateList) {
        Map<String, List> map = loadExcelFileService.regionDataMap;

        // List를 돌면서 각 지역별로 TM좌표 저장
        for(TmCoordinate tm : tmCoordinateList) {
            String fullRegionName = tm.getSidoName() + tm.getSggName() + tm.getUmdName();
            fullRegionName = fullRegionName.replaceAll(" ", "");

            List valueList = map.get(fullRegionName);
            valueList.add(tm.getTmX());
            valueList.add(tm.getTmY());
        }
    }
}