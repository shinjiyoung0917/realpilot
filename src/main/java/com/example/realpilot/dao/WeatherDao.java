package com.example.realpilot.dao;

import com.example.realpilot.dgraph.DgraphOperations;
import com.example.realpilot.model.weather.HourlyWeather;
import com.example.realpilot.model.weather.WeatherRootQuery;
import com.example.realpilot.model.weather.Weathers;
import com.google.gson.Gson;
import io.dgraph.DgraphClient;
import io.dgraph.DgraphProto;
import io.dgraph.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
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

    public void createWeatherNode(HourlyWeather hourlyWeather) {
        Transaction transaction = dgraphClient.newTransaction();
        operations.mutate(transaction, hourlyWeather);

    }

    public HourlyWeather getHourlyWeatherNode(String uid, String baseDate, String baseTime) {
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
        WeatherRootQuery weatherRootQuery = gson.fromJson(res.getJson().toStringUtf8(), WeatherRootQuery.class);
        List<Weathers> hourlyWeatherOfRegion =  weatherRootQuery.getHourlyWeatherOfRegion();

        return hourlyWeatherOfRegion.get(0).getHourlyWeathers().get(0);
    }
}


/*
지역-날짜에 의한 날씨 찾는 쿼리
{
  hourlyWeatherByRegion as var(func: eq(umdName, "장충동")) {
    hourlyWeathers {
      expand(_all_)
  	}
  }

  hourlyWeatherByRegionAndDate(func: uid(hourlyWeatherByRegion)) {
    uid
    hourlyWeathers {
      uid
      expand(_all_)
    }
  }
}
*/