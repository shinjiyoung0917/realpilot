package com.example.realpilot.dao;

import com.example.realpilot.dgraph.DgraphOperations;
import com.example.realpilot.model.weather.*;
import com.example.realpilot.service.DateService;
import com.example.realpilot.utilAndConfig.*;
import com.google.gson.Gson;
import io.dgraph.DgraphClient;
import io.dgraph.DgraphProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WeatherDao<T> {
    private static final Logger log = LoggerFactory.getLogger(WeatherDao.class);

    @Autowired
    private DateService dateService;
    @Autowired
    private DateDao dateDao;

    @Autowired
    private DgraphClient dgraphClient;
    @Autowired
    private DgraphOperations operations;
    @Autowired
    private Gson gson = new Gson();

    public WeatherRootQuery getAlreadyExistingWeatherNodeWithRegionUidAndDate(String uid, Map<DateUnit, Integer> dateMap, Query query) {
        Map<String, String> var = new LinkedHashMap<>();
        var.put("$id", uid);
        // TODO: 날짜, 시간 파라미터 설정하는 로직 중복 해결하기
        var.put("$year", String.valueOf(dateMap.get(DateUnit.YEAR)));
        var.put("$month", String.valueOf(dateMap.get(DateUnit.MONTH)));
        var.put("$day", String.valueOf(dateMap.get(DateUnit.DAY)));
        var.put("$hour", String.valueOf(dateMap.get(DateUnit.HOUR)));

        String dateQueryString = dateDao.getDateQueryString(DateUnit.HOUR, var, dateMap, query);
        String fullQueryString = queryStringWithRegionUidAndDate(dateQueryString, query.getRootQuery(), query.getEdge());

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(fullQueryString, var);
        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);

        return weatherRootQuery;
    }

    private String queryStringWithRegionUidAndDate(String dateQueryString, String weatherRootQuery, String weatherEdge) {
        String fullQueryString = "query region($id: string, $year: int, $month: int, $day: int, $hour: int) {\n" +
                " region(func: uid($id)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    sggName\n" +
                "    umdName\n" +
                "    var1 as " + weatherEdge + " {\n" +
                "      uid\n" +
                "    }\n" +
                "  }\n" +
                "    \n" +
                dateQueryString +
                "    \n" +
                "  " + weatherRootQuery + "(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    uid\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return fullQueryString;
    }

    public Optional<HourlyWeather> getHourlyWeatherNodeLinkedToRegionWithRegionUidAndDate(String uid, String forecastDate, String forecastTime, Query query) {
        DgraphProto.Response res = null;
        res = queryByForecastDateAndTime(uid, forecastDate, forecastTime, query.getRootQuery(), query.getEdge());

        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);
        List<HourlyWeather> hourlyWeatherResult =  weatherRootQuery.getHourlyWeather();

        Optional<HourlyWeather> result = Optional.empty();
        // TODO: 연쇄적인 null 체크는 map으로 수정
        if(Optional.ofNullable(hourlyWeatherResult).isPresent() && !hourlyWeatherResult.isEmpty()) {
            List<HourlyWeather> hourlyWeatherList = hourlyWeatherResult.get(0).getHourlyWeathers();
            if(Optional.ofNullable(hourlyWeatherList).isPresent() && !hourlyWeatherList.isEmpty()) {
                result = Optional.of(hourlyWeatherList.get(0));
            }
        }

        return result;
    }

    public WeatherRootQuery getAlreadyExistingWeatherNodeWithRegionNameAndDate(String sidoName, String sggName, String umdName, RegionUnit regionUnit, DateUnit dateUnit, Query query) {
        // TODO; 서비스에서 하도록
        Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();

        String fullQueryString = "";
        Map<String, String> var = new LinkedHashMap<>();

        String rootQuery = query.getRootQuery();
        String edge = query.getEdge();

        // TODO: 날짜, 시간 파라미터 설정하는 로직 중복 해결하기
        var.put("$year", String.valueOf(dateMap.get(DateUnit.YEAR)));
        var.put("$month", String.valueOf(dateMap.get(DateUnit.MONTH)));
        var.put("$day", String.valueOf(dateMap.get(DateUnit.DAY)));

        String dateQueryString = dateDao.getDateQueryString(dateUnit, var, dateMap, query);

        switch (regionUnit) {
            case SIDO:
                fullQueryString = queryStringWithSidoNameAndDate(dateQueryString, rootQuery, edge);
                sidoName = "/.*" + sidoName + ".*/i";
                var.put("$sidoName", sidoName);
                break;
            case SIDO_SGG:
                fullQueryString = queryStringWithSidoAndSggNameAndDate(dateQueryString, rootQuery, edge);
                sidoName = "/.*" + sidoName + ".*/i";
                var.put("$sidoName", sidoName);
                var.put("$sggName", sggName);
                break;
            case SIDO_UMD:
                fullQueryString = queryStringWithSidoAndUmdNameAndDate(dateQueryString, rootQuery, edge);
                sidoName = "/.*" + sidoName + ".*/i";
                var.put("$sidoName", sidoName);
                var.put("$umdName", umdName);
                break;
            case SIDO_SGG_UMD:
                fullQueryString = queryStringWithSidoAndSggAndUmdNameAndDate(dateQueryString, rootQuery, edge);
                sidoName = "/.*" + sidoName + ".*/i";
                var.put("$sidoName", sidoName);
                var.put("$sggName", sggName);
                var.put("$umdName", umdName);
                break;
        }

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(fullQueryString, var);
        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);

        return weatherRootQuery;
    }

    // TODO: 아래에 있는 지역 별로 조회하는 쿼리문들 하나로 합쳐도 될 듯?
    private String queryStringWithSidoNameAndDate(String dateQueryString, String weatherRootQuery, String weatherEdge) {
        String fullQueryString = "query region($sidoName: string, $year: int, $month: int, $day: int, $hour: int) {\n" +
                " region(func: regexp(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    var1 as " + weatherEdge + " {\n" +
                "      uid\n" +
                "    }\n" +
                "  }\n" +
                "    \n" +
                dateQueryString +
                "    \n" +
                "  " + weatherRootQuery + "(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    uid\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return fullQueryString;
    }

    private String queryStringWithSidoAndSggNameAndDate(String dateQueryString, String weatherRootQuery, String weatherEdge) {
        String fullQueryString = "query region($sidoName: string, $sggName: string, $year: int, $month: int, $day: int, $hour: int) {\n" +
                " region(func: regexp(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    sigungus @filter(eq(sggName, $sggName)) {\n" +
                "      uid\n" +
                "      sggName\n" +
                "      var1 as " + weatherEdge + " {\n" +
                "        uid\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "    \n" +
                dateQueryString +
                "    \n" +
                "  " + weatherRootQuery + "(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return fullQueryString;
    }

    private String queryStringWithSidoAndUmdNameAndDate(String dateQueryString, String weatherRootQuery, String weatherEdge) {
        String fullQueryString = "query region($sidoName: string, $umdName: string, $year: int, $month: int, $day: int, $hour: int) {\n" +
                " region(func: regexp(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    eubmyeondongs @filter(eq(umdName, $umdName)) {\n" +
                "      uid\n" +
                "      umdName\n" +
                "      var1 as " + weatherEdge + " {\n" +
                "        uid\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "    \n" +
                dateQueryString +
                "      \n" +
                "  " + weatherRootQuery + "(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return fullQueryString;
    }

    private String queryStringWithSidoAndSggAndUmdNameAndDate(String dateQueryString, String weatherRootQuery, String weatherEdge) {
        String fullQueryString = "query region($sidoName: string, $sggName: string, $umdName: string, $year: int, $month: int, $day: int, $hour: int) {\n" +
                " region(func: regexp(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    sigungus @filter(eq(sggName, $sggName)) {\n" +
                "      uid\n" +
                "      sggName\n" +
                "      eubmyeondongs @filter(eq(umdName, $umdName)) {\n" +
                "        uid\n" +
                "        umdName\n" +
                "        var1 as  " + weatherEdge + " {\n" +
                "          uid\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "    \n" +
                dateQueryString +
                "      \n" +
                "  " + weatherRootQuery + "(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return fullQueryString;
    }

    public Optional<DailyWeather> getDailyWeatherNodeLinkedToRegionWithRegionUidAndDate(String uid, String forecastDate) {
        DgraphProto.Response res;

        res = queryForKweatherDay7OrAmPm7(uid, forecastDate, Query.DAILY_WEATHER.getRootQuery(), Query.DAILY_WEATHER.getEdge());

        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);
        List<DailyWeather> dailyWeatherResult =  weatherRootQuery.getDailyWeather();

       Optional<DailyWeather> result = Optional.empty();
        if(Optional.ofNullable(dailyWeatherResult).isPresent() && !dailyWeatherResult.isEmpty()) {
            List<DailyWeather> dailyWeatherList = dailyWeatherResult.get(0).getDailyWeathers();
            if(Optional.ofNullable(dailyWeatherList).isPresent() && !dailyWeatherList.isEmpty()) {
                result = Optional.of(dailyWeatherList.get(0));
            }
        }

        return result;
    }

    public Optional<AmWeather> getAmWeatherNodeLinkedToRegionWithRegionUidAndDate(String uid, String forecastDate) {
        DgraphProto.Response res;

        res = queryForKweatherDay7OrAmPm7(uid, forecastDate, Query.AM_WEATHER.getRootQuery(), Query.AM_WEATHER.getEdge());

        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);
        List<AmWeather> amWeatherResult =  weatherRootQuery.getAmWeather();

        Optional<AmWeather> result = Optional.empty();
        if(Optional.ofNullable(amWeatherResult).isPresent() && !amWeatherResult.isEmpty()) {
            List<AmWeather> amWeatherList = amWeatherResult.get(0).getAmWeathers();
            if(Optional.ofNullable(amWeatherList).isPresent() && !amWeatherList.isEmpty()) {
                result = Optional.of(amWeatherList.get(0));
            }
        }

        return result;
    }

    public Optional<PmWeather> getPmWeatherNodeLinkedToRegionWithRegionUidAndDate(String uid, String forecastDate) {
        DgraphProto.Response res;

        res = queryForKweatherDay7OrAmPm7(uid, forecastDate, Query.PM_WEATHER.getRootQuery(), Query.PM_WEATHER.getEdge());

        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);
        List<PmWeather> pmWeatherResult =  weatherRootQuery.getPmWeather();

        Optional<PmWeather> result = Optional.empty();
        if(Optional.ofNullable(pmWeatherResult).isPresent() && !pmWeatherResult.isEmpty()) {
            List<PmWeather> pmWeatherList = pmWeatherResult.get(0).getPmWeathers();
            if(Optional.ofNullable(pmWeatherList).isPresent() && !pmWeatherList.isEmpty()) {
                result = Optional.of(pmWeatherList.get(0));
            }
        }

        return result;
    }

    private DgraphProto.Response queryForKweatherDay7OrAmPm7(String uid, String forecastDate, String rootQuery, String edge) {
        String fullQueryString = "query " + rootQuery + "($id: string, $forecastDate: string) {\n" +
                "  " + rootQuery + "(func: uid($id)) {\n" +
                "    " + edge + " @filter(eq(forecastDate, $forecastDate)) {\n" +
                "      uid\n" +
                "      expand(_all_)\n" +
                "    }\n" +
                "  }\n" +
                "}";

        Map<String, String> var = new LinkedHashMap<>();
        var.put("$id", uid);
        var.put("$forecastDate", forecastDate);

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(fullQueryString, var);

        return res;
    }

    public Optional<SpecialWeather> getSpecialWeatherNodeLinkedToRegionWithRegionUidAndDate(String uid, String forecastDate, String forecastTime) {
        DgraphProto.Response res;

        res = queryByForecastDateAndTime(uid, forecastDate, forecastTime, Query.SPECIAL_WEATHER.getRootQuery(), Query.SPECIAL_WEATHER.getEdge());

        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);
        List<SpecialWeather> specialWeatherResult =  weatherRootQuery.getSpecialWeather();

        Optional<SpecialWeather> result = Optional.empty();
        if(Optional.ofNullable(specialWeatherResult).isPresent() && !specialWeatherResult.isEmpty()) {
            List<SpecialWeather> specialWeatherList = specialWeatherResult.get(0).getSpecialWeathers();
            if(Optional.ofNullable(specialWeatherList).isPresent() && !specialWeatherList.isEmpty()) {
                result = Optional.of(specialWeatherList.get(0));
            }
        }

        return result;
    }

    private DgraphProto.Response queryByForecastDateAndTime(String uid, String forecastDate, String forecastTime, String rootQuery, String edge) {
        String fullQueryString = "query " + rootQuery + "($id: string, $forecastDate: string, $forecastTime: string) {\n" +
                " " + rootQuery + "(func: uid($id)) {\n" +
                "    " + edge + " @filter(eq(forecastDate, $forecastDate) and eq(forecastTime, $forecastTime)) {\n" +
                "      uid\n" +
                "      expand(_all_)\n" +
                "    }\n" +
                "  }\n" +
                "}";

        Map<String, String> var = new LinkedHashMap<>();
        var.put("$id", uid);
        var.put("$forecastDate", forecastDate);
        var.put("$forecastTime", forecastTime);

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(fullQueryString, var);

        return res;
    }

    public void updateWeatherNode(T weather) {
        operations.mutate(dgraphClient.newTransaction(), weather);
    }
}