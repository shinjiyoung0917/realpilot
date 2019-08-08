package com.example.realpilot.dao;

import com.example.realpilot.dgraph.DgraphOperations;
import com.example.realpilot.model.airPollution.AirPollutionDetail;
import com.example.realpilot.model.airPollution.AirPollutionOverall;
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

    public AirPollutionRootQuery getAlreadyExistingAirPollutionNodeWithRegionUidAndDate(String uid, Map<DateUnit, Integer> dateMap, String airPollutionCode, DateUnit dateUnit, Query query) {
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
        var.put("$airPollutionCode", airPollutionCode);

        String dateQueryString = dateDao.getDateQueryString(dateUnit, var, dateMap, query);
        String fullQueryString = queryStringWithRegionUidAndDate(dateQueryString, rootQuery, edge);

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(fullQueryString, var);
        AirPollutionRootQuery airPollutionRootQuery = gson.fromJson(res.getJson().toStringUtf8(), AirPollutionRootQuery.class);

        return airPollutionRootQuery;
    }

    public String queryStringWithRegionUidAndDate(String dateQueryString, String airPollutionRootQuery, String airPollutionEdge) {
        String fullQueryString = "query region($id: string, $year: int, $month: int, $day: int, $hour: int, $airPollutionCode: string) {\n" +
                " region(func: uid($id)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    sggName\n" +
                "    umdName\n";
        if(airPollutionRootQuery.equals(Query.AIR_POLLUTION_OVERALL.getRootQuery())) {
            fullQueryString += "    var1 as " + airPollutionEdge + " @filter(eq(airPollutionCode, $airPollutionCode)) {\n";
        } else {
            fullQueryString += "    var1 as " + airPollutionEdge + " {\n";
        }
        fullQueryString +=
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

    public Optional<AirPollutionDetail> getAirPollutionDetailNodeLinkedToRegionWithRegionUidAndDate(String uid, String forecastDate, String forecastTime) {
        DgraphProto.Response res;

        res = queryForRealTimeAirPollution(uid, forecastDate, forecastTime);

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

    public DgraphProto.Response queryForRealTimeAirPollution(String uid, String forecastDate, String forecastTime) {
        String fullQueryString = "query airPollutionDetail($id: string, $forecastDate: string, $forecastTime: string) {\n" +
                " airPollutionDetail(func: uid($id)) {\n" +
                "    airPollutionDetails @filter(eq(forecastDate, $forecastDate) and eq(forecastTime, $forecastTime)) {\n" +
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

    public Optional<AirPollutionOverall> getAirPollutionOverallNodeLinkedToRegionWithRegionUidAndDate(String uid, String forecastDate, String forecastTime, String airPollutionCode) {
        DgraphProto.Response res;
        res = queryForAirPollutionForecastOverall(uid, forecastDate, forecastTime, airPollutionCode);

        AirPollutionRootQuery airPollutionRootQuery = gson.fromJson(res.getJson().toStringUtf8(), AirPollutionRootQuery.class);
        List<AirPollutionOverall> airPollutionOverallResult =  airPollutionRootQuery.getAirPollutionOverall();

        Optional<AirPollutionOverall> result = Optional.empty();
        if(Optional.ofNullable(airPollutionOverallResult).isPresent() && !airPollutionOverallResult.isEmpty()) {
            List<AirPollutionOverall> airPollutionOverallList = airPollutionOverallResult.get(0).getAirPollutionOveralls();
            if(Optional.ofNullable(airPollutionOverallList).isPresent() && !airPollutionOverallList.isEmpty()) {
                result = Optional.of(airPollutionOverallList.get(0));
            }
        }

        return result;
    }

    private DgraphProto.Response queryForAirPollutionForecastOverall(String uid, String forecastDate, String forecastTime, String airPollutionCode) {
        Map<String, String> var = new LinkedHashMap<>();
        var.put("$id", uid);
        var.put("$forecastDate", forecastDate);
        var.put("$airPollutionCode", airPollutionCode);

        String fullQueryString = "query airPollutionOverall($id: string, $forecastDate: string, $forecastTime: string, $airPollutionCode: string) {\n" +
                " airPollutionOverall(func: uid($id)) {\n" +
                "    airPollutionOveralls @filter(eq(forecastDate, $forecastDate) ";
        if(!forecastTime.equals("")) {
            var.put("$forecastTime", forecastTime);
            fullQueryString += "and eq(forecastTime, $forecastTime) ";
        }
        fullQueryString += " and eq(airPollutionCode, $airPollutionCode)) {\n" +
                "      uid\n" +
                "      expand(_all_)\n" +
                "    }\n" +
                "  }\n" +
                "}";

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(fullQueryString, var);

        return res;
    }


    public void updateAirPollutionNode(T airPollution) {
        operations.mutate(dgraphClient.newTransaction(), airPollution);
    }


}
