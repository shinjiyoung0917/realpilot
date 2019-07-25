package com.example.realpilot.dao;

import com.example.realpilot.dgraph.DgraphOperations;
import com.example.realpilot.excelModel.RegionData;
import com.example.realpilot.model.region.*;
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
public class RegionDao {
    private static final Logger log = LoggerFactory.getLogger(RegionDao.class);

    @Autowired
    private DgraphClient dgraphClient;
    @Autowired
    private DgraphOperations operations;
    @Autowired
    private Gson gson = new Gson();

    public void createRegionNode(Transaction transaction, Map<String, RegionData> regionDataMap) {
        Sido prevSavedSido = new Sido();
        Sigungu prevSavedSigungu = new Sigungu();
        Regions region = new Regions();

        for(String regionName: regionDataMap.keySet()) {
            // regionDataMap의 list 타입의 value => 행정동코드, 생성날짜, 격자X,Y, TM X,Y
            RegionData regionData = regionDataMap.get(regionName);

            if(regionData.getSggName().equals("") && regionData.getUmdName().equals("")) {
                Sido sido = new Sido();
                prevSavedSido = sido.setRegion(regionData);
                region.getSidos().add(prevSavedSido);
            } else if(regionData.getSggName().equals("") && !regionData.getUmdName().equals("")) { // '세종특별자치시'의 경우 시군구 이름이 빈 칸이기 때문에 읍면동으로 바로 연결해주기 위함
                Eubmyeondong eubmyeondong = new Eubmyeondong();
                eubmyeondong.setRegion(prevSavedSido, regionData);
            } else if(!regionData.getSggName().equals("") && regionData.getUmdName().equals("")) {
                Sigungu sigungu = new Sigungu();
                prevSavedSigungu = sigungu.setRegion(prevSavedSido, regionData);
            } else {
                Eubmyeondong eubmyeondong = new Eubmyeondong();
                eubmyeondong.setRegion(prevSavedSigungu, regionData);
            }
        }

        operations.mutate(transaction, region);
    }

    public List<Regions> getRegionNodeWithGrid(Integer gridX, Integer gridY) {
        String query = "query regionByGrid($gridX: int, $gridY: int) {\n" +
                " regionByGrid(func: eq(gridX, $gridX)) @filter(eq(gridY, $gridY)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    sggName\n" +
                "    umdName\n" +
                "    gridX\n" +
                "    gridY\n" +
                "  }\n" +
                "}";

        Map<String, String> var  = new LinkedHashMap<>();
        var.put("$gridX", String.valueOf(gridX));
        var.put("$gridY", String.valueOf(gridY));

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(query, var);
        RegionRootQuery regionRootQuery = gson.fromJson(res.getJson().toStringUtf8(), RegionRootQuery.class);
        List<Regions> regionByGrid =  regionRootQuery.getRegionByGrid();

        return regionByGrid;
    }

    public List<Regions> getRegionNodeWithUid(String uid) {
        String query = "query {\n" +
                " regionByUid(func:uid($uid)) {\n" +
                "    expand(_all_)\n" +
                "  }\n" +
                "}";

        Map<String, String> var = Collections.singletonMap("$uid", String.valueOf(uid));

        DgraphProto.Response res = dgraphClient.newTransaction().query(query);
        RegionRootQuery regionRootQuery = gson.fromJson(res.getJson().toStringUtf8(), RegionRootQuery.class);
        List<Regions> regionByUid =  regionRootQuery.getRegionByUid();

        return regionByUid;
    }

    public void updateRegionNode(Regions region) {
        Transaction transaction = dgraphClient.newTransaction();
        operations.mutate(transaction, region);
    }
}
