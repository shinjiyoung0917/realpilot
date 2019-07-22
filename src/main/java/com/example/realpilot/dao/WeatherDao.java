package com.example.realpilot.dao;

import com.example.realpilot.dgraph.DgraphOperations;
import com.example.realpilot.model.weather.DailyWeather;
import com.example.realpilot.model.weather.HourlyWeather;
import com.example.realpilot.model.weather.WeatherRootQuery;
import com.example.realpilot.model.weather.Weathers;
import com.example.realpilot.service.DateService;
import com.example.realpilot.utilAndConfig.DateUnit;
import com.example.realpilot.utilAndConfig.ExternalWeatherApi;
import com.example.realpilot.utilAndConfig.RegionUnit;
import com.google.gson.Gson;
import io.dgraph.DgraphClient;
import io.dgraph.DgraphProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class WeatherDao {
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
        List<Weathers> hourlyWeather =  weatherRootQuery.getHourlyWeather();

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

    public DailyWeather getDailyWeatherNodeByRegionAndDate(String sidoName, String sggName, String umdName, RegionUnit regionUnit) {
        Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();

        String query = "";
        Map<String, String> var = new LinkedHashMap<>();

        switch (regionUnit) {
            case SIDO:
                query = queryBySidoNameAndDate();
                var.put("$sidoName", sidoName);
                break;
            case SIDO_SGG:
                query = queryBySidoAndSggNameAndDate();
                var.put("$sidoName", sidoName);
                var.put("$sggName", sggName);
                break;
            case SIDO_UMD:
                query = queryBySidoAndUmdNameAndDate();
                var.put("$sidoName", sidoName);
                var.put("$umdName", umdName);
                break;
            case SIDO_SGG_UMD:
                query = queryBySidoAndSggAndUmdNameAndDate();
                var.put("$sidoName", sidoName);
                var.put("$sggName", sggName);
                var.put("$umdName", umdName);
                break;
        }

        // TODO: 날짜, 시간 파라미터 설정하는 로직 중복 해결하기
        var.put("$year", String.valueOf(dateMap.get(DateUnit.YEAR)));
        var.put("$month", String.valueOf(dateMap.get(DateUnit.MONTH)));
        var.put("$day", String.valueOf(dateMap.get(DateUnit.DAY)));
        var.put("$hour", String.valueOf(dateMap.get(DateUnit.HOUR)));

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(query, var);
        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);
        List<Weathers> dailyWeather =  weatherRootQuery.getDailyWeather();

        return dailyWeather.get(0).getDailyWeathers().get(0);
    }

    private String queryBySidoNameAndDate() {
        String query = "query regionByName($sidoName: string) {\n" + //, $year: int, $month: int, $day: int, $hour: int
                " regionByName(func: regexp(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    var1 as dailyWeathers {\n" +
                "      uid\n" +
                "    }\n" +
                "  }\n" +
                "    \n" +
                queryForDate() +
                "      \n" +
                "  dailyWeather(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return query;
    }

    private String queryBySidoAndSggNameAndDate() {
        String query = "query regionByName($sidoName: string, $sggName: string) {\n" +
                " regionByName(func: regexp(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    sigungus @filter(regexp(sggName, $sggName)) {\n" +
                "      var1 as dailyWeathers {\n" +
                "        uid\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n" +
                "    \n" +
                queryForDate() +
                "      \n" +
                "  dailyWeather(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return query;
    }

    private String queryBySidoAndUmdNameAndDate() {
        String query = "query regionByName($sidoName: string, $umdName: string) {\n" +
                " regionByName(func: regexp(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    eubmyeondongs @filter(regexp(umdName, $umdName)) {\n" +
                "      var1 as dailyWeathers {\n" +
                "        uid\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n" +
                "    \n" +
                queryForDate() +
                "      \n" +
                "  dailyWeather(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return query;
    }

    private String queryBySidoAndSggAndUmdNameAndDate() {
        String query = "query regionByName($sidoName: string, $sggName: string, $umdName: string) {\n" +
                " regionByName(func: regexp(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    sigungus @filter(regexp(sggName, $sggName)) {\n" +
                "      uid\n" +
                "      sggName\n" +
                "      eubmyeondongs @filter(regexp(umdName, $umdName)) {\n" +
                "        var1 as dailweathers {\n" +
                "          uid\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n" +
                "    \n" +
                queryForDate() +
                "      \n" +
                "  dailyWeather(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return query;
    }

    private String queryForDate() {
        /*String query =
                "  date(func: eq(year, $year)) {\n" +
                "    year\n" +
                "    months @filter(eq(month, $month)) {\n" +
                "      month\n" +
                "      days @filter(eq(day, $day)) {\n" +
                "        day\n" +
                "        hours @filter(eq(hour, $hour)) {\n" +
                "          hour\n" +
                "          var2 as dailyWeathers {\n" +
                "            uid\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n";*/

        String query =
                "  date(func: eq(year, 2019)) {\n" +
                        "    year\n" +
                        "    months @filter(eq(month, 7)) {\n" +
                        "      month\n" +
                        "      days @filter(eq(day, 22)) {\n" +
                        "        day\n" +
                        "        hours @filter(eq(hour, 18)) {\n" +
                        "          hour\n" +
                        "          var2 as dailyWeathers {\n" +
                        "            uid\n" +
                        "          }\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n";

        return query;
    }
}


    /*public DailyWeather getDailyWeatherNodeBySidoNameAndDate(String sidoName) {
        Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();

        String query = "query regionByName($sidoName: string) {\n" +
                " regionByName(func: regexp(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    var1 as dailyWeathers {\n" +
                "      uid\n" +
                "    }\n" +
                "  }\n" +
                "    \n" +
                queryForDate() +
                "      \n" +
                "  dailWeather(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        Map<String, String> var = new LinkedHashMap<>();
        var.put("$sidoName", sidoName);
        // TODO: 날짜, 시간 파라미터 설정하는 로직 중복 해결하기
        var.put("$year", String.valueOf(dateMap.get(DateUnit.YEAR)));
        var.put("$month", String.valueOf(dateMap.get(DateUnit.MONTH)));
        var.put("$day", String.valueOf(dateMap.get(DateUnit.DAY)));
        var.put("$hour", String.valueOf(dateMap.get(DateUnit.HOUR)));

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(query, var);
        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);
        List<Weathers> dailyWeather =  weatherRootQuery.getDailyWeather();

        return dailyWeather.get(0).getDailyWeathers().get(0);
    }

    public DailyWeather getDailyWeatherNodeBySidoAndSggNameAndDate(String sidoName, String sggName) {
        Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();

        String query = "query regionByName($sidoName: string, $sggName: string) {\n" +
                " regionByName(func: regexp(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    sigungus @filter(regexp(sggName, $sggName)) {\n" +
                "      var1 as dailyWeathers {\n" +
                "        uid\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n" +
                "    \n" +
                queryForDate() +
                "      \n" +
                "  dailWeather(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        Map<String, String> var = new LinkedHashMap<>();
        var.put("$sidoName", sidoName);
        var.put("$sggName", sggName);
        // TODO: 날짜, 시간 파라미터 설정하는 로직 중복 해결하기
        var.put("$year", String.valueOf(dateMap.get(DateUnit.YEAR)));
        var.put("$month", String.valueOf(dateMap.get(DateUnit.MONTH)));
        var.put("$day", String.valueOf(dateMap.get(DateUnit.DAY)));
        var.put("$hour", String.valueOf(dateMap.get(DateUnit.HOUR)));

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(query, var);
        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);
        List<Weathers> dailyWeather =  weatherRootQuery.getDailyWeather();

        return dailyWeather.get(0).getDailyWeathers().get(0);
    }

    public DailyWeather getDailyWeatherNodeBySidoAndUmdNameAndDate(String sidoName, String umdName) {
        Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();

        String query = "query regionByName($sidoName: string, $umdName: string) {\n" +
                " regionByName(func: regexp(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    eubmyeondongs @filter(regexp(umdName, $umdName)) {\n" +
                "      var1 as dailyWeathers {\n" +
                "        uid\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n" +
                "    \n" +
                queryForDate() +
                "      \n" +
                "  dailWeather(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        Map<String, String> var = new LinkedHashMap<>();
        var.put("$sidoName", sidoName);
        var.put("$umdName", umdName);
        // TODO: 날짜, 시간 파라미터 설정하는 로직 중복 해결하기
        var.put("$year", String.valueOf(dateMap.get(DateUnit.YEAR)));
        var.put("$month", String.valueOf(dateMap.get(DateUnit.MONTH)));
        var.put("$day", String.valueOf(dateMap.get(DateUnit.DAY)));
        var.put("$hour", String.valueOf(dateMap.get(DateUnit.HOUR)));

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(query, var);
        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);
        List<Weathers> dailyWeather =  weatherRootQuery.getDailyWeather();

        return dailyWeather.get(0).getDailyWeathers().get(0);
    }

    public DailyWeather getDailyWeatherNodeBySidoAndSggAndUmdNameAndDate(String sidoName, String sggName, String umdName) {
        Map<DateUnit, Integer> dateMap = dateService.getCurrentDate();

        String query = "query regionByName($sidoName: string, $sggName: string, $umdName: string) {\n" +
                " regionByName(func: regexp(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    sigungus @filter(regexp(sggName, $sggName)) {\n" +
                "      uid\n" +
                "      sggName\n" +
                "      eubmyeondongs @filter(regexp(umdName, $umdName)) {\n" +
                "        var1 as dailweathers {\n" +
                "          uid\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n" +
                "    \n" +
                queryForDate() +
                "      \n" +
                "  dailWeather(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        Map<String, String> var = new LinkedHashMap<>();
        var.put("$sidoName", sidoName);
        var.put("$sggName", sggName);
        var.put("$umdName", umdName);
        // TODO: 날짜, 시간 파라미터 설정하는 로직 중복 해결하기
        var.put("$year", String.valueOf(dateMap.get(DateUnit.YEAR)));
        var.put("$month", String.valueOf(dateMap.get(DateUnit.MONTH)));
        var.put("$day", String.valueOf(dateMap.get(DateUnit.DAY)));
        var.put("$hour", String.valueOf(dateMap.get(DateUnit.HOUR)));

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(query, var);
        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);
        List<Weathers> dailyWeather =  weatherRootQuery.getDailyWeather();

        return dailyWeather.get(0).getDailyWeathers().get(0);
    }
    */


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