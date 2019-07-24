package com.example.realpilot.dao;

import com.example.realpilot.model.date.*;
import com.example.realpilot.dgraph.DgraphOperations;
import com.example.realpilot.utilAndConfig.DateUnit;
import com.google.gson.Gson;
import io.dgraph.DgraphClient;
import io.dgraph.DgraphProto;
import io.dgraph.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DateDao<T> {
    private static final Logger log = LoggerFactory.getLogger(DateDao.class);

    @Autowired
    private DgraphClient dgraphClient;
    @Autowired
    private DgraphOperations operations;
    @Autowired
    private Gson gson = new Gson();

    private static final int TOTAL_TIME_OF_DAY = 24;
    private static final int TOTAL_MONTHS_OF_YEAR = 12;

    // TODO: 해당 메서드 다른 클래스로 이동 (dgraph config 관련쪽으로?)
    public void createSchema(DgraphClient dgraphClient) {
        // ** DB ALTER ** //
        dgraphClient.alter(DgraphProto.Operation.newBuilder().setDropAll(true).build());

        String schema = "year: int @index(int) .\n" +
                "month: int @index(int) .\n" +
                "day: int @index(int) .\n" +
                "hour: int @index(int) .\n" +
                "sidoName: string @index(fulltext, trigram) .\n" +
                "sggName: string @index(fulltext, trigram) .\n" +
                "umdName: string @index(fulltext, trigram) .\n" +
                "hCode: int @index(int) .\n" +
                "createdDate: int @index(int) .\n" +
                "gridX: int @index(int) .\n" +
                "gridY: int @index(int) .\n" +
                "tmX: float @index(float) .\n" +
                "tmY: float @index(float) .\n" +
                "baseDate: string @index(fulltext) .\n" +
                "baseTime: string @index(fulltext) .\n" +
                "fcstDate: string @index(fulltext) .\n" +
                "fcstTime: string @index(fulltext) .\n" +
                "tm: string @index(fulltext) .\n" +
                "hourlyWeathers: uid @reverse .\n" +
                "dailyWeathers: uid @reverse .";

        DgraphProto.Operation op = DgraphProto.Operation.newBuilder().setSchema(schema).build();
        dgraphClient.alter(op);

        log.info("[Dao] createSchema - DGraph 스키마 세팅 완료");
    }

    public int getDateNodeCount(DgraphClient dgraphClient) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        String query = "query monthsCount($year: int) {\n" +
                " monthsCount(func: eq(year, $year)) {\n" +
                "    countOfMonths: count(months)\n" +
                "  }\n" +
                "}";

        Map<String, String> var = Collections.singletonMap("$year", String.valueOf(currentYear));
        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(query, var);

        DateRootQuery dateRootQuery = gson.fromJson(res.getJson().toStringUtf8(), DateRootQuery.class);

        List<DateRootQuery.DataByFunc> monthsCount = dateRootQuery.getMonthsCount();
        if(!monthsCount.isEmpty()) {
            return monthsCount.get(0).getCountOfMonths();
        } else {
            log.info("[Dao] getDateNodeCountDao - DB에 날짜 노드 없음");
            return 0;
        }
    }

    public void createDateNode(Transaction transaction) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        List<Hour> hourList = new ArrayList<>();
        for(int h = 0 ; h < TOTAL_TIME_OF_DAY ; ++h) {
            Hour hour = new Hour();
            hour.setDate(h);
            hourList.add(hour);
        }

        List<Month> monthList = new ArrayList<>();
        for(int m = 1 ; m <= TOTAL_MONTHS_OF_YEAR ; ++m) {
            calendar.set(currentYear, m - 1, 1);
            int daysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            log.info("[Dao] createDateNode - " + m + "월의 일 수 : " + daysOfMonth);

            List<Day> dayList = new ArrayList<>();
            for (int d = 1; d <= daysOfMonth; ++d) {
                Day day = new Day();
                day.setDate(d, hourList);
                dayList.add(day);
            }

            Month month = new Month();
            month.setDate(m, dayList);
            monthList.add(month);
        }

        Year year = new Year();
        year.setDate(currentYear, monthList);

        Dates date = new Dates();
        date.getYears().add(year);

        operations.mutate(transaction, date);
    }

    public Hour getHourNode(Map<DateUnit, Integer> dateMap) {
        String query = "query currentDate($year: int, $month: int, $day: int, $hour: int) {\n" +
                " currentDate(func: eq(year, $year)) {\n" +
                "    year\n" +
                "    months @filter(eq(month, $month)) {\n" +
                "      month\n" +
                "      days @filter(eq(day, $day)) {\n" +
                "        day\n" +
                "        hours @filter(eq(hour, $hour)) {\n" +
                "          uid\n" +
                "          hour\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        Map<String, String> var  = new LinkedHashMap<>();
        var.put("$year", String.valueOf(dateMap.get(DateUnit.YEAR)));
        var.put("$month", String.valueOf(dateMap.get(DateUnit.MONTH)));
        var.put("$day", String.valueOf(dateMap.get(DateUnit.DAY)));
        var.put("$hour", String.valueOf(dateMap.get(DateUnit.HOUR)));

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(query, var);
        DateRootQuery dateRootQuery = gson.fromJson(res.getJson().toStringUtf8(), DateRootQuery.class);
        List<Dates> currentDate =  dateRootQuery.getCurrentDate();

        // TODO: null 체크, Optional로 반환
        return currentDate.get(0).getMonths().get(0).getDays().get(0).getHours().get(0);
    }

    public Day getDayNode(Map<DateUnit, Integer> dateMap) {
        String query = "query currentDate($year: int, $month: int, $day: int) {\n" +
                " currentDate(func: eq(year, $year)) {\n" +
                "    year\n" +
                "    months @filter(eq(month, $month)) {\n" +
                "      month\n" +
                "      days @filter(eq(day, $day)) {\n" +
                "        uid\n" +
                "        day\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        Map<String, String> var  = new LinkedHashMap<>();
        var.put("$year", String.valueOf(dateMap.get(DateUnit.YEAR)));
        var.put("$month", String.valueOf(dateMap.get(DateUnit.MONTH)));
        var.put("$day", String.valueOf(dateMap.get(DateUnit.DAY)));

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(query, var);
        DateRootQuery dateRootQuery = gson.fromJson(res.getJson().toStringUtf8(), DateRootQuery.class);
        List<Dates> currentDate =  dateRootQuery.getCurrentDate();

        // TODO: null 체크, Optional로 반환
        return currentDate.get(0).getMonths().get(0).getDays().get(0);
    }

    public void updateDateNode(T date) {
        Transaction transaction = dgraphClient.newTransaction();
        operations.mutate(transaction, date);
    }
}
