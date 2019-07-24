package com.example.realpilot.dao;

import com.example.realpilot.dgraph.DgraphOperations;
import com.example.realpilot.model.weather.DailyWeather;
import com.example.realpilot.model.weather.HourlyWeather;
import com.example.realpilot.model.weather.WeatherRootQuery;
import com.example.realpilot.service.DateService;
import com.example.realpilot.utilAndConfig.*;
import com.google.gson.Gson;
import io.dgraph.DgraphClient;
import io.dgraph.DgraphProto;
import io.dgraph.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class WeatherDao<T> {
    private static final Logger log = LoggerFactory.getLogger(WeatherDao.class);

    @Autowired
    private DateService dateService;

    @Autowired
    private DgraphClient dgraphClient;
    @Autowired
    private DgraphOperations operations;
    @Autowired
    private Gson gson = new Gson();

    public HourlyWeather getHourlyWeatherNodeByDate(String uid, String date, String time, ExternalWeatherApi api) {
        DgraphProto.Response res = null;

        if(api.equals(ExternalWeatherApi.FORECAST_GRIB)) {
            res = queryForForecastGrib(uid, date, time);
        } else if(api.equals(ExternalWeatherApi.FORECAST_TIME) || api.equals(ExternalWeatherApi.FORECAST_SPACE)) {
            res = queryForForecastTimeOrSpace(uid, date, time);
        }

        // TODO: DB에서 가져온 res 객체 null일 경우 처리
        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);
        List<HourlyWeather> hourlyWeather =  weatherRootQuery.getHourlyWeather();

        // TODO: null 체크, Optional로 반환
        return hourlyWeather.get(0).getHourlyWeathers().get(0);
    }

    private DgraphProto.Response queryForForecastGrib(String uid, String baseDate, String baseTime) {
        String query = "query hourlyWeather($id: string, $baseDate: string, $baseTime: string) {\n" +
                " hourlyWeather(func: uid($id)) {\n" +
                "    hourlyWeathers @filter(eq(baseDate, $baseDate) and eq(baseTime, $baseTime)) {\n" +
                "      uid\n" +
                "      expand(_all_)\n" +
                "    }\n" +
                "  }\n" +
                "}";

        Map<String, String> var = new LinkedHashMap<>();
        var.put("$id", uid);
        var.put("$baseDate", baseDate);
        var.put("$baseTime", baseTime);

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(query, var);

        return res;
    }

    private DgraphProto.Response queryForForecastTimeOrSpace(String uid, String fcstDate, String fcstTime) {
        String query = "query hourlyWeather($id: string, $fcstDate: string, $fcstTime: string) {\n" +
                " hourlyWeather(func: uid($id)) {\n" +
                "    hourlyWeathers @filter(eq(fcstDate, $fcstDate) and eq(fcstTime, $fcstTime)) {\n" +
                "      uid\n" +
                "      expand(_all_)\n" +
                "    }\n" +
                "  }\n" +
                "}";

        Map<String, String> var = new LinkedHashMap<>();
        var.put("$id", uid);
        var.put("$fcstDate", fcstDate);
        var.put("$fcstTime", fcstTime);

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(query, var);

        return res;
    }

    public WeatherRootQuery getHourlyWeatherNodeByRegionAndDate(String uid) {
        Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();

        String query = queryByRegionUidAndDate(queryByHour(), WeatherEdge.HOURLY_WEATHER.getWeatherEdge(), RootQuery.HOURLY_WEATHER_ROOT_QUERY.getRootQuery());
        Map<String, String> var = new LinkedHashMap<>();

        var.put("$id", uid);
        // TODO: 날짜, 시간 파라미터 설정하는 로직 중복 해결하기
        var.put("$year", String.valueOf(dateMap.get(DateUnit.YEAR)));
        var.put("$month", String.valueOf(dateMap.get(DateUnit.MONTH)));
        var.put("$day", String.valueOf(dateMap.get(DateUnit.DAY)));
        var.put("$hour", String.valueOf(dateMap.get(DateUnit.HOUR)));

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(query, var);
        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);

        return weatherRootQuery;
    }

    private String queryByRegionUidAndDate(String dateQuery, String weatherEdge, String weatherRootQuery) {
        String query = "query region($id: string, $year: int, $month: int, $day: int, $hour: int) {\n" +
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
                dateQuery +
                "    \n" +
                "  " + weatherRootQuery + "(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    uid\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return query;
    }

    public Optional getDailyWeatherNodeByDate(String uid, String date) {
        DgraphProto.Response res;

        res = queryForKweatherDay7(uid, date);

        // TODO: DB에서 가져온 res 객체 null일 경우 처리
        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);
        List<DailyWeather> dailyWeatherResult =  weatherRootQuery.getDailyWeather();

       Optional<DailyWeather> result = Optional.empty();
        if(!dailyWeatherResult.isEmpty()) {
            List<DailyWeather> dailyWeatherList = dailyWeatherResult.get(0).getDailyWeathers();
            if(!dailyWeatherList.isEmpty()) {
                result = Optional.of(dailyWeatherList.get(0));
            }
        }

        return result;
    }

    private DgraphProto.Response queryForKweatherDay7(String uid, String date) {
        String query = "query dailyWeather($id: string, $date: string) {\n" +
                "  dailyWeather(func: uid($id)) {\n" +
                "    dailyWeathers @filter(eq(tm, $date)) {\n" +
                "      uid\n" +
                "      expand(_all_)\n" +
                "    }\n" +
                "  }\n" +
                "}";

        Map<String, String> var = new LinkedHashMap<>();
        var.put("$id", uid);
        var.put("$date", date);

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(query, var);

        return res;
    }

    public WeatherRootQuery getDailyWeatherNodeByRegionAndDate(String sidoName, String sggName, String umdName, RegionUnit regionUnit) {
        Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();

        String query = "";
        Map<String, String> var = new LinkedHashMap<>();

        switch (regionUnit) {
            case SIDO:
                query = queryBySidoNameAndDate(queryByDay(), WeatherEdge.DAILY_WEATHER.getWeatherEdge(), RootQuery.DAILY_WEATHER_ROOT_QUERY.getRootQuery());
                sidoName = "/.*" + sidoName + ".*/i";
                var.put("$sidoName", sidoName);
                break;
            case SIDO_SGG:
                query = queryBySidoAndSggNameAndDate(queryByDay(), WeatherEdge.DAILY_WEATHER.getWeatherEdge(), RootQuery.DAILY_WEATHER_ROOT_QUERY.getRootQuery());
                sidoName = "/.*" + sidoName + ".*/i";
                //sggName = "/.*" + sggName + ".*/i";
                var.put("$sidoName", sidoName);
                var.put("$sggName", sggName);
                break;
            case SIDO_UMD:
                query = queryBySidoAndUmdNameAndDate(queryByDay(), WeatherEdge.DAILY_WEATHER.getWeatherEdge(), RootQuery.DAILY_WEATHER_ROOT_QUERY.getRootQuery());
                sidoName = "/.*" + sidoName + ".*/i";
                //umdName = "/.*" + umdName + ".*/i";
                var.put("$sidoName", sidoName);
                var.put("$umdName", umdName);
                break;
            case SIDO_SGG_UMD:
                query = queryBySidoAndSggAndUmdNameAndDate(queryByDay(), WeatherEdge.DAILY_WEATHER.getWeatherEdge(), RootQuery.DAILY_WEATHER_ROOT_QUERY.getRootQuery());
                sidoName = "/.*" + sidoName + ".*/i";
                //sggName = "/.*" + sggName + ".*/i";
                //umdName = "/.*" + umdName + ".*/i";
                var.put("$sidoName", sidoName);
                var.put("$sggName", sggName);
                var.put("$umdName", umdName);
                break;
        }

        // TODO: 날짜, 시간 파라미터 설정하는 로직 중복 해결하기
        var.put("$year", String.valueOf(dateMap.get(DateUnit.YEAR)));
        var.put("$month", String.valueOf(dateMap.get(DateUnit.MONTH)));
        var.put("$day", String.valueOf(dateMap.get(DateUnit.DAY)));

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(query, var);
        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);

        return weatherRootQuery;
    }

    private String queryBySidoNameAndDate(String dateQuery, String weatherEdge, String weatherRootQuery) {
        String query = "query region($sidoName: string, $year: int, $month: int, $day: int, $hour: int) {\n" +
                " region(func: regexp(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    var1 as " + weatherEdge + " {\n" +
                "      uid\n" +
                "    }\n" +
                "  }\n" +
                "    \n" +
                dateQuery +
                "    \n" +
                "  " + weatherRootQuery + "(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    uid\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return query;
    }

    private String queryBySidoAndSggNameAndDate(String dateQuery, String weatherEdge, String weatherRootQuery) {
        String query = "query region($sidoName: string, $sggName: string, $year: int, $month: int, $day: int) {\n" +
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
                dateQuery +
                "    \n" +
                "  " + weatherRootQuery + "(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return query;
    }

    private String queryBySidoAndUmdNameAndDate(String dateQuery, String weatherEdge, String weatherRootQuery) {
        String query = "query region($sidoName: string, $umdName: string, $year: int, $month: int, $day: int) {\n" +
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
                dateQuery +
                "      \n" +
                "  " + weatherRootQuery + "(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return query;
    }

    private String queryBySidoAndSggAndUmdNameAndDate(String dateQuery, String weatherEdge, String weatherRootQuery) {
        String query = "query region($sidoName: string, $sggName: string, $umdName: string, $year: int, $month: int, $day: int) {\n" +
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
                dateQuery +
                "      \n" +
                "  " + weatherRootQuery + "(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return query;
    }

    private String queryByHour() {
        String query =
                "  date(func: eq(year, $year)) {\n" +
                "    year\n" +
                "    months @filter(eq(month, $month)) {\n" +
                "      month\n" +
                "      days @filter(eq(day, $day)) {\n" +
                "        day\n" +
                "        hours @filter(eq(hour, $hour)) {\n" +
                "          uid\n" +
                "          hour\n" +
                "          var2 as hourlyWeathers {\n" +
                "            uid\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n";

        return query;
    }

    private String queryByDay() {
        String query =
                "  date(func: eq(year, $year)) {\n" +
                "    year\n" +
                "    months @filter(eq(month, $month)) {\n" +
                "      month\n" +
                "      days @filter(eq(day, $day)) {\n" +
                "        uid\n" +
                "        day\n" +
                "        var2 as dailyWeathers {\n" +
                "          uid\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n";

        return query;
    }

    public void updateWeatherNode(T weather) {
        Transaction transaction = dgraphClient.newTransaction();
        operations.mutate(transaction, weather);
    }
}


/*private String queryBySidoNameAndDateOfDailyWeather() {
        String query = "query regionByName($sidoName: string, $year: int, $month: int, $day: int) {\n" +
                " regionByName(func: regexp(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    var1 as dailyWeathers {\n" +
                "      uid\n" +
                "    }\n" +
                "  }\n" +
                "    \n" +
                queryByDay() +
                "    \n" +
                "  dailyWeather(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    uid\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return query;
    }

    private String queryBySidoAndSggNameAndDateOfDailyWeather() {
        String query = "query regionByName($sidoName: string, $sggName: string, $year: int, $month: int, $day: int) {\n" +
                " regionByName(func: regexp(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    sigungus @filter(regexp(sggName, $sggName)) {\n" +
                "      var1 as dailyWeathers {\n" +
                "        uid\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "    \n" +
                queryByDay() +
                "    \n" +
                "  dailyWeather(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return query;
    }

    private String queryBySidoAndUmdNameAndDateOfDailyWeather() {
        String query = "query regionByName($sidoName: string, $umdName: string, $year: int, $month: int, $day: int) {\n" +
                " regionByName(func: regexp(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    eubmyeondongs @filter(regexp(umdName, $umdName)) {\n" +
                "      var1 as dailyWeathers {\n" +
                "        uid\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "    \n" +
                queryByDay() +
                "      \n" +
                "  dailyWeather(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return query;
    }

    private String queryBySidoAndSggAndUmdNameAndDateOfDailyWeather() {
        String query = "query regionByName($sidoName: string, $sggName: string, $umdName: string, $year: int, $month: int, $day: int) {\n" +
                " regionByName(func: regexp(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    sigungus @filter(regexp(sggName, $sggName)) {\n" +
                "      uid\n" +
                "      sggName\n" +
                "      eubmyeondongs @filter(regexp(umdName, $umdName)) {\n" +
                "        var1 as dailyWeathers {\n" +
                "          uid\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "    \n" +
                queryByHour() +
                "      \n" +
                "  dailyWeather(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return query;
    }*/


//regexp(sggName, /.*용인시.*처인.*/i)


/*
{
  dailyWeatherByRegion(func: regexp(sidoName, /.서울./i)) {
    uid
    sidoName
    var1 as hourlyWeathers {
      uid
    }
  }

  dailyWeatherByDate(func: eq(year, 2019)) {
    year
    months @filter(eq(month, 7)) {
      month
      days @filter(eq(day, 18)){
        day
        var2 as hourlyWeathers {
          uid
        }
      }
    }
  }

  dailyWeatherByRegionAndDate(func: uid(var1)) @filter(uid(var2)) {
    expand(_all_)
  }

}
*/

/*
지역과 날짜에 의한 날씨 찾는 쿼리
{
  hourlyWeatherByRegion(func: eq(umdName, "장충동")) {
    uid
    umdName
    var1 as hourlyWeathers {
      uid
    }
  }

  hourlyWeatherByDate(func: eq(year, 2019)) {
    year
    months @filter(eq(month, 7)) {
      month
      days @filter(eq(day, 18)) {
        day
        hours @filter(eq(hour, 11)) {
          hour
          var2 as hourlyWeathers {
            uid
          }
        }
      }
    }
  }

  hourlyWeatherByRegionAndDate(func: uid(var1)) @filter(uid(var2)) {
    expand(_all_)
  }
}
*/