package com.example.realpilot.service;

import com.example.realpilot.externalApiModel.tmCoordinate.OpenModel;
import com.example.realpilot.externalApiModel.tmCoordinate.TmCoordinate;
import com.example.realpilot.utilAndConfig.SidoList;
import com.example.realpilot.utilAndConfig.WxMappingJackson2HttpMessageConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.*;
import java.util.*;

@Service
public class LoadTmCoordinateService<T> {
    private static final Logger log = LoggerFactory.getLogger(LoadTmCoordinateService.class);

    @Autowired
    private LoadExcelFileService loadExcelFileService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${tmCoordinate.api.url}")
    private String tmCoordinateApiUrl;
    @Value("${api.serviceKey}")
    private String serviceKey;

    public void callTmCoordinateApi() {
        restTemplate.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());

        // TODO: null말고 new로 초기화 해주기
        OpenModel openModel = null;
        for(SidoList sido : SidoList.values()) {
            log.info("[Service] callTmCoordinateApi - 시도 이름 : " + sido.getSidoName());

            URI uri = URI.create(tmCoordinateApiUrl + "?ServiceKey=" + serviceKey + "&umdName=" + sido.getSidoName() + "&numOfRows=" + 600 + "&_returnType=json");

            try {
                openModel = restTemplate.getForObject(uri, OpenModel.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            parseTmCoordinate(openModel.getList());
        }

    }

    private void parseTmCoordinate(List<TmCoordinate> tmCoordinateList) {
        Map<String, List> regionDataMap = loadExcelFileService.regionDataMap;

        // List를 돌면서 각 지역별로 TM좌표 저장
        for(TmCoordinate tm : tmCoordinateList) {
            String fullRegionName = tm.getSidoName() + tm.getSggName() + tm.getUmdName();
            fullRegionName = fullRegionName.replaceAll(" ", "");

            Optional<List<T>> optionalValueList = Optional.ofNullable(regionDataMap.get(fullRegionName));
            if(optionalValueList.isPresent()) {
                optionalValueList.get().add((T)(Object)tm.getTmX());
                optionalValueList.get().add((T)(Object)tm.getTmY());
            }
        }
    }
}