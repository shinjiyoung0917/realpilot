package com.example.realpilot.dao;

import com.example.realpilot.dgraph.DgraphOperations;
import com.example.realpilot.model.disaster.DisasterRootQuery;
import com.example.realpilot.model.disaster.Earthquake;
import com.example.realpilot.utilAndConfig.DateUnit;
import com.example.realpilot.utilAndConfig.Query;
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
import java.util.Optional;

@Repository
public class DisasterDao<T> {
    private static final Logger log = LoggerFactory.getLogger(DisasterDao.class);

    @Autowired
    private DateDao dateDao;

    @Autowired
    private DgraphClient dgraphClient;
    @Autowired
    private DgraphOperations operations;
    @Autowired
    private Gson gson = new Gson();

    public DisasterRootQuery getAlreadyExistingDisasterNodeWithRegionUidAndDate(String uid, Map<DateUnit, Integer> dateMap, DateUnit dateUnit, Query query) {
        String rootQuery = query.getRootQuery();
        String edge = query.getEdge();

        Map<String, String> var = new LinkedHashMap<>();
        var.put("$id", uid);
        // TODO: 날짜, 시간 파라미터 설정하는 로직 중복 해결하기
        var.put("$year", String.valueOf(dateMap.get(DateUnit.YEAR)));
        var.put("$month", String.valueOf(dateMap.get(DateUnit.MONTH)));
        var.put("$day", String.valueOf(dateMap.get(DateUnit.DAY)));
        if(dateUnit.equals(DateUnit.HOUR)) {
            var.put("$hour", String.valueOf(dateMap.get(DateUnit.HOUR)));
        }

        String dateQueryString = dateDao.getDateQueryString(dateUnit, var, dateMap, query);
        String fullQueryString = queryStringWithRegionUidAndDate(dateQueryString, rootQuery, edge);

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(fullQueryString, var);
        DisasterRootQuery disasterRootQuery = gson.fromJson(res.getJson().toStringUtf8(), DisasterRootQuery.class);

        return disasterRootQuery;
    }

    // TODO: WeatherDao랑 중복 해결
    public String queryStringWithRegionUidAndDate(String dateQueryString, String rootQuery, String edge) {
        String fullQueryString = "query region($id: string, $year: int, $month: int, $day: int, $hour: int) {\n" +
                " region(func: uid($id)) {\n" +
                "    uid\n" +
                "    countryName\n" +
                "    sidoName\n" +
                "    sggName\n" +
                "    umdName\n" +
                "    var1 as " + edge + " {\n" +
                "      uid\n" +
                        "    }\n" +
                        "  }\n" +
                        "    \n" +
                        dateQueryString +
                        "    \n" +
                        "  " + rootQuery + "(func: uid(var1)) @filter(uid(var2)) {\n" +
                        "    uid\n" +
                        "    expand(_all_)\n" +
                        "  }\n" +
                        "}";

        return fullQueryString;
    }

    public Optional<Earthquake> getEarthquakeNodeLinkedToRegionWithRegionUidAndDate(String uid, String releaseDate, String releaseTime) {
        DgraphProto.Response res;
        res = queryForEarthquake(uid, releaseDate, releaseTime);

        DisasterRootQuery disasterRootQuery = gson.fromJson(res.getJson().toStringUtf8(), DisasterRootQuery.class);
        List<Earthquake> earthquakeResult =  disasterRootQuery.getEarthquake();

        Optional<Earthquake> result = Optional.empty();
        if(Optional.ofNullable(earthquakeResult).isPresent() && !earthquakeResult.isEmpty()) {
            List<Earthquake> earthquakeList = earthquakeResult.get(0).getEarthquakes();
            if(Optional.ofNullable(earthquakeList).isPresent() && !earthquakeList.isEmpty()) {
                result = Optional.of(earthquakeList.get(0));
            }
        }

        return result;
    }

   private DgraphProto.Response queryForEarthquake(String uid, String occurrenceDate, String occurrenceTime) {
        Map<String, String> var = new LinkedHashMap<>();
        var.put("$id", uid);
        var.put("$occurrenceDate", occurrenceDate);
       var.put("$occurrenceTime", occurrenceTime);

        String fullQueryString = "query earthquake($id: string, $occurrenceDate: string, $occurrenceTime: string) {\n" +
                " earthquake(func: uid($id)) {\n" +
                "    earthquakes @filter(eq(occurrenceDate, $occurrenceDate) and eq(occurrenceTime, $occurrenceTime) {\n" +
                "      uid\n" +
                "      expand(_all_)\n" +
                "    }\n" +
                "  }\n" +
                "}";

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(fullQueryString, var);

        return res;
    }

    public void updateDisasterNode(T disaster) {
        operations.mutate(dgraphClient.newTransaction(), disaster);
    }
}
