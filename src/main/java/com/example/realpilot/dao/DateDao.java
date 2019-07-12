package com.example.realpilot.dao;

import com.example.realpilot.model.date.DateQueryResult;
import com.example.realpilot.model.date.Year;
import com.example.realpilot.dgraph.DgraphOperations;
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
public class DateDao {
    private static final Logger log = LoggerFactory.getLogger(DateDao.class);

    @Autowired
    private Gson gson = new Gson();
    @Autowired
    private DgraphOperations operations;

    // TODO: 해당 메서드 다른 클래스로 이동
    public void createSchema(DgraphClient dgraphClient) {
        // ** DB ALTER ** //
        dgraphClient.alter(DgraphProto.Operation.newBuilder().setDropAll(true).build());

        String schema = "year: int @index(int) .\n"
                + "month: int @index(int) .\n"
                + "day: int @index(int) .\n"
                + "hour: int @index(int) .\n"
                + "sidoName: string @index(fulltext) .\n"
                + "sggName: string @index(fulltext) .\n"
                + "umdName: string @index(fulltext) .\n"
                + "hCode: int @index(int) .\n"
                + "createdDate: int @index(int) .\n"
                + "gridX: int @index(int) .\n"
                + "gridY: int @index(int) .\n"
                + "tmX: float @index(float) .\n"
                + "tmY: float @index(float) .\n";

        DgraphProto.Operation op = DgraphProto.Operation.newBuilder().setSchema(schema).build();
        dgraphClient.alter(op);

        log.info("[Dao] createSchema - DGraph 스키마 세팅 완료");
    }

    public int getDateNodeCountDao(DgraphClient dgraphClient) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(calendar.YEAR);

        String query = "query monthsCount($year: int) {\n" +
                " monthsCount(func: eq(year, $year)) {\n" +
                "    countOfMonths: count(months)\n" +
                "  }\n" +
                "}";
        Map<String, String> var = Collections.singletonMap("$year", String.valueOf(currentYear));
        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(query, var);

        DateQueryResult dateQueryResult = gson.fromJson(res.getJson().toStringUtf8(), DateQueryResult.class);

        List<DateQueryResult.DataByFunc> monthsCount = dateQueryResult.getMonthsCount();
        if(monthsCount.size() != 0) {
            return monthsCount.get(0).getCountOfMonths();
        } else {
            log.info("[Dao] getDateNodeCountDao - DB에 날짜 노드 없음");
            return 0;
        }
    }

    public void createDateNode(Transaction transaction) {
        Year year = new Year();

        operations.mutate(transaction, year.setDate());
    }
}
