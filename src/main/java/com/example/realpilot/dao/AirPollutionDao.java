package com.example.realpilot.dao;

import com.example.realpilot.model.airPollution.AirPollutionRootQuery;
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
import java.util.Map;

@Repository
public class AirPollutionDao {
    private static final Logger log = LoggerFactory.getLogger(AirPollutionDao.class);

    @Autowired
    private DateDao dateDao;

    @Autowired
    private DgraphClient dgraphClient;
    @Autowired
    private Gson gson = new Gson();

    public AirPollutionRootQuery getAlreadyExistingAirPollutionDeatailNodeWithMeasureStationInfoAndDate(String measureStationName, String measureStationAddr, Map<DateUnit, Integer> dateMap, DateUnit dateUnit, Query query) {
        String rootQuery = query.getRootQuery();
        String edge = query.getEdge();

        Map<String, String> var = new LinkedHashMap<>();
        var.put("$measureStationName", measureStationName);
        var.put("$measureStationAddr", measureStationAddr);
        // TODO: 날짜, 시간 파라미터 설정하는 로직 중복 해결하기
        var.put("$year", String.valueOf(dateMap.get(DateUnit.YEAR)));
        var.put("$month", String.valueOf(dateMap.get(DateUnit.MONTH)));
        var.put("$day", String.valueOf(dateMap.get(DateUnit.DAY)));
        var.put("$hour", String.valueOf(dateMap.get(DateUnit.HOUR)));

        String dateQueryString = dateDao.getDateQueryString(dateUnit, var, dateMap);
        String fullQueryString = queryWithMeasureStationInfoAndDate(dateQueryString, rootQuery, edge);

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(fullQueryString, var);
        AirPollutionRootQuery airPollutionRootQuery = gson.fromJson(res.getJson().toStringUtf8(), AirPollutionRootQuery.class);

        return airPollutionRootQuery;
    }

    public String queryWithMeasureStationInfoAndDate(String dateQueryString, String airPollutionRootQuery, String airPollutionEdge) {
        String fullQueryString = "query region($measureStationName: string, $measureStationAddr: string, $year: int, $month: int, $day: int, $hour: int) {\n" +
                " region(func: eq(measureStationName, $measureStationName)) @filter(eq(measureStationAddr, $measureStationAddr)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    sggName\n" +
                "    umdName\n" +
                "    var1 as " + airPollutionEdge + " {\n" +
                "      uid\n" +
                "    }\n" +
                "  }\n" +
                "    \n" +
                dateQueryString +
                "    \n" +
                "  " + airPollutionRootQuery + "(func: uid(var1)) @filter(uid(var2)) {\n" +
                "    uid\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        return fullQueryString;
    }
}
