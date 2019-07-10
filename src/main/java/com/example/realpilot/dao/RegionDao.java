package com.example.realpilot.dao;

import com.example.realpilot.dgraph.DgraphOperations;
import com.example.realpilot.model.region.Eubmyeondong;
import com.example.realpilot.model.region.Region;
import com.example.realpilot.model.region.Sido;
import com.example.realpilot.model.region.Sigungu;
import com.example.realpilot.utilAndConfig.RegionListIndex;
import com.google.gson.Gson;
import io.dgraph.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class RegionDao<T> {
    private static final Logger log = LoggerFactory.getLogger(RegionDao.class);

    @Autowired
    private Gson gson = new Gson();
    @Autowired
    private DgraphOperations operations;

    public void createRegionNode(Transaction transaction, Map<String, List<T>> regionDataMap) {
        Sido prevSavedSido = new Sido();
        Sigungu prevSavedSigungu = new Sigungu();
        Region region = new Region();

        for(String regionName: regionDataMap.keySet()) {
            // 행정동코드, 생성날짜, 격자X,Y, TM X,Y
            List<T> valueList = regionDataMap.get(regionName);
            String sggName = valueList.get(RegionListIndex.SIGUNGU_NAME_INDEX.getListIndex()).toString();
            String umdName = valueList.get(RegionListIndex.EUBMYEONDONG_NAME_INDEX.getListIndex()).toString();

            if(sggName.equals("")) {
                Sido sido = new Sido();
                prevSavedSido = sido.setRegion(valueList);
                region.getSidos().add(prevSavedSido);
            } else if(umdName.equals("")) {
                Sigungu sigungu = new Sigungu();
                prevSavedSigungu = sigungu.setRegion(prevSavedSido, valueList);
            } else {
                Eubmyeondong eubmyeondong = new Eubmyeondong();
                eubmyeondong.setRegion(prevSavedSigungu, valueList);
            }
        }

        operations.mutate(transaction, region);
    }
}
