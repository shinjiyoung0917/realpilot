package com.example.realpilot;

import com.example.realpilot.dao.WeatherDao;
import com.example.realpilot.model.region.Regions;
import com.example.realpilot.model.weather.WeatherRootQuery;
import com.example.realpilot.service.WeatherService;
import com.example.realpilot.utilAndConfig.DateUnit;
import com.example.realpilot.utilAndConfig.Query;
import com.example.realpilot.utilAndConfig.RegionUnit;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class WeatherServiceTest {
    @MockBean
    private WeatherService weatherService;
    @MockBean
    private WeatherDao weatherDao;

    @Test
    public void test지역이름으로지역노드찾아날씨노드연결() {
        String sidoName = "서울특별시";
        String sggName = "서초구";
        String umdName = "서초동";
        Map<DateUnit, Integer> dateMap  = new HashMap<>();
        dateMap.put(DateUnit.YEAR, 2019);
        dateMap.put(DateUnit.MONTH, 8);
        dateMap.put(DateUnit.DAY, 16);
        dateMap.put(DateUnit.HOUR, 17);

        WeatherRootQuery weatherRootQuery = weatherDao.getAlreadyExistingWeatherNodeOfKoreaWithRegionNameAndDate(sidoName, sggName, umdName, dateMap, RegionUnit.SIDO_SGG_UMD, DateUnit.HOUR, Query.HOURLY_WEATHER);
        AtomicReference<Regions> region =  new AtomicReference<>();
        Optional.ofNullable(weatherRootQuery.getRegion()).ifPresent(notNullRegion -> region.set(notNullRegion.get(0)));

        //
    }
}
