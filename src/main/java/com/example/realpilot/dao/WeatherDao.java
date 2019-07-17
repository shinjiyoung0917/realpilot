package com.example.realpilot.dao;

import com.example.realpilot.dgraph.DgraphOperations;
import com.example.realpilot.model.weather.HourlyWeather;
import com.example.realpilot.model.weather.WeatherRootQuery;
import com.example.realpilot.model.weather.Weathers;
import com.example.realpilot.utilAndConfig.ExternalWeatherApi;
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

@Repository
public class WeatherDao {
    private static final Logger log = LoggerFactory.getLogger(WeatherDao.class);

    @Autowired
    private DgraphClient dgraphClient;
    @Autowired
    private DgraphOperations operations;
    @Autowired
    private Gson gson = new Gson();

    public HourlyWeather getHourlyWeatherNode(String uid, String date, String time, ExternalWeatherApi api) {
        DgraphProto.Response res = null;
        if(api.equals(ExternalWeatherApi.FORECAST_GRIB)) {
            res = queryForForecastGrib(uid, date, time);
        } else if(api.equals(ExternalWeatherApi.FORECAST_TIME)) {
            res = queryForForecastTime(uid, date, time);
        }
        // TODO: DB에서 가져온 res 객체 null일 경우 처리
        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);
        List<Weathers> hourlyWeatherOfRegion =  weatherRootQuery.getHourlyWeatherOfRegion();

        return hourlyWeatherOfRegion.get(0).getHourlyWeathers().get(0);
    }

    private DgraphProto.Response queryForForecastGrib(String uid, String baseDate, String baseTime) {
        String query = "query hourlyWeatherOfRegion($id: string, $baseDate: string, $baseTime: string) {\n" +
                " hourlyWeatherOfRegion(func: uid($id)) {\n" +
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

    private DgraphProto.Response queryForForecastTime(String uid, String fcstDate, String fcstTime) {
        String query = "query hourlyWeatherOfRegion($id: string, $fcstDate: string, $fcstTime: string) {\n" +
                " hourlyWeatherOfRegion(func: uid($id)) {\n" +
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
}


/*
지역-날짜에 의한 날씨 찾는 쿼리
{
  hourlyWeatherByRegion as var1(func: eq(umdName, "장충동")) {
    hourlyWeathers {
      expand(_all_)
  	}
  }

hourlyWeatherByDate as var2(func: eq(year, 2019)) {
    months @filter(eq(month, 7)) {
      days @filter(eq(day, 17)) {
        hours @filter(eq(hour, 11)) {
          hourlyWeathers {
          	expand(_all_)
          }
        }
      }
    }
  }

  hourlyWeatherByRegionAndDate(func: uid(hourlyWeatherByRegion, hourlyWeatherByDate)) {
    uid
    year
    month
    day
    hour
    sidoName
    sggName
    umdName
    hourlyWeathers {
      expand(_all_)
    }
  }
}
*/