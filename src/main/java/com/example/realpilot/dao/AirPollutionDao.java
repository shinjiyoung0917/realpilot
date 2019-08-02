package com.example.realpilot.dao;

import com.example.realpilot.dgraph.DgraphOperations;
import com.example.realpilot.model.airPollution.AirPollutionDetail;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class AirPollutionDao<T> {
    private static final Logger log = LoggerFactory.getLogger(AirPollutionDao.class);

    @Autowired
    private DateDao dateDao;

    @Autowired
    private DgraphClient dgraphClient;
    @Autowired
    private DgraphOperations operations;
    @Autowired
    private Gson gson = new Gson();

    public AirPollutionRootQuery getAlreadyExistingAirPollutionDeatailNodeWithMeasureStationInfoAndDate(String measureStationName, Map<DateUnit, Integer> dateMap, DateUnit dateUnit, Query query) {
        String rootQuery = query.getRootQuery();
        String edge = query.getEdge();

        Map<String, String> var = new LinkedHashMap<>();
        var.put("$measureStationName", measureStationName);
        // TODO: 날짜, 시간 파라미터 설정하는 로직 중복 해결하기
        var.put("$year", String.valueOf(dateMap.get(DateUnit.YEAR)));
        var.put("$month", String.valueOf(dateMap.get(DateUnit.MONTH)));
        var.put("$day", String.valueOf(dateMap.get(DateUnit.DAY)));
        var.put("$hour", String.valueOf(dateMap.get(DateUnit.HOUR)));

        String dateQueryString = dateDao.getDateQueryString(dateUnit, var, dateMap, Query.AIR_POLLUTION_DETAIL);
        String fullQueryString = queryWithMeasureStationInfoAndDate(dateQueryString, rootQuery, edge);

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(fullQueryString, var);
        AirPollutionRootQuery airPollutionRootQuery = gson.fromJson(res.getJson().toStringUtf8(), AirPollutionRootQuery.class);

        return airPollutionRootQuery;
    }

    public String queryWithMeasureStationInfoAndDate(String dateQueryString, String airPollutionRootQuery, String airPollutionEdge) {
        String fullQueryString = "query region($measureStationName: string, $year: int, $month: int, $day: int, $hour: int) {\n" +
                " region(func: eq(measureStationName, $measureStationName)) {\n" +
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

    public Optional<AirPollutionDetail> getAirPollutionDetailNodeLinkedToRegionWithRegionUidAndDate(String uid, String date, String time) {
        DgraphProto.Response res;

        res = queryForRealTimeAirPollutionInfo(uid, date, time);

        AirPollutionRootQuery airPollutionRootQuery = gson.fromJson(res.getJson().toStringUtf8(), AirPollutionRootQuery.class);
        List<AirPollutionDetail> airPollutionDetailResult =  airPollutionRootQuery.getAirPollutionDetail();

        Optional<AirPollutionDetail> result = Optional.empty();
        if(Optional.ofNullable(airPollutionDetailResult).isPresent() && !airPollutionDetailResult.isEmpty()) {
            List<AirPollutionDetail> airPollutionDetailList = airPollutionDetailResult.get(0).getAirPollutionDetails();
            if(Optional.ofNullable(airPollutionDetailList).isPresent() && !airPollutionDetailList.isEmpty()) {
                result = Optional.of(airPollutionDetailList.get(0));
            }
        }

        return result;
    }

    public DgraphProto.Response queryForRealTimeAirPollutionInfo(String uid, String date, String time) {
        String fullQueryString = "query airPollutionDetail($id: string, $date: string, $time: string) {\n" +
                " airPollutionDetail(func: uid($id)) {\n" +
                "    airPollutionDetails @filter(eq(date, $date) and eq(time, $time)) {\n" +
                "      uid\n" +
                "      expand(_all_)\n" +
                "    }\n" +
                "  }\n" +
                "}";

        Map<String, String> var = new LinkedHashMap<>();
        var.put("$id", uid);
        var.put("$date", date);
        var.put("$time", time);

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(fullQueryString, var);

        return res;
    }

    public void updateAirPollutionNode(T airPollution) {
        operations.mutate(dgraphClient.newTransaction(), airPollution);
    }
}
