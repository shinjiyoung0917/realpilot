package com.example.realpilot.dao;

import com.example.realpilot.dgraph.DgraphOperations;
import com.example.realpilot.excelModel.RegionData;
import com.example.realpilot.model.region.Eubmyeondong;
import com.example.realpilot.model.region.Region;
import com.example.realpilot.model.region.Sido;
import com.example.realpilot.model.region.Sigungu;
import com.google.gson.Gson;
import io.dgraph.DgraphClient;
import io.dgraph.DgraphProto;
import io.dgraph.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Map;

@Repository
public class RegionDao {
    private static final Logger log = LoggerFactory.getLogger(RegionDao.class);

    @Autowired
    private DgraphClient dgraphClient;
    @Autowired
    private Gson gson = new Gson();
    @Autowired
    private DgraphOperations operations;

    public void createRegionNode(Transaction transaction, Map<String, RegionData> regionDataMap) {
        Sido prevSavedSido = new Sido();
        Sigungu prevSavedSigungu = new Sigungu();
        Region region = new Region();

        for(String regionName: regionDataMap.keySet()) {
            // 행정동코드, 생성날짜, 격자X,Y, TM X,Y
            RegionData regionData = regionDataMap.get(regionName);
            String sggName = regionData.getSggName();
            String umdName = regionData.getUmdName();

            if(sggName.equals("")) {
                Sido sido = new Sido();
                prevSavedSido = sido.setRegion(regionData);
                region.getSidos().add(prevSavedSido);
            } else if(umdName.equals("")) {
                Sigungu sigungu = new Sigungu();
                prevSavedSigungu = sigungu.setRegion(prevSavedSido, regionData);
            } else {
                Eubmyeondong eubmyeondong = new Eubmyeondong();
                eubmyeondong.setRegion(prevSavedSigungu, regionData);
            }
        }

        operations.mutate(transaction, region);
    }

    public Region getRegionNodeByGrid(Integer gridX, Integer gridY) {
        String query = "query regionByGrid(gridX: int) {\n" +
                " regionByGrid(func: eq(gridX, $gridX)) @fileter(eq(gridY, $gridY) {\n" +
                " expand(_all_)\n" +
                " }\n" +
                " }";
        Map<String, String> var = Collections.singletonMap("gridX", String.valueOf(gridX));
        var.put("gridY", String.valueOf(gridY));
        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(query, var);

        Region region = gson.fromJson(res.getJson().toStringUtf8(), Region.class);

        //List<Region.RegionByGrid> regionByGrid = region.getRegionByGrid();

        return region;
    }
}
