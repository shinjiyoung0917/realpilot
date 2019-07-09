package com.example.realpilot.dao;

import com.example.realpilot.model.region.Sido;
import com.example.realpilot.model.region.Sigungu;
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

    public void createRegionNode(Transaction transaction, Map<String, List<T>> regionDataMap) {
        Sido sido = new Sido();
        Sigungu sigungu = new Sigungu();

        for(String regionName: regionDataMap.keySet()) {
            log.info("[Dao] createRegionNode - ");

            // 행정동코드, 생성날짜, 격자X,Y, TM X,Y
            List<T> valueList = regionDataMap.get(regionName);
            String sidoName = valueList.get(0).toString();
            String sggName = valueList.get(1).toString();
            String umdName = valueList.get(2).toString();

            String sidoSggUmdName = sidoName + sggName  + umdName;
            sidoSggUmdName = sidoSggUmdName.replaceAll(" ", "");
            String sidoSggName = sidoName + sggName  + umdName;
            sidoSggName = sidoSggName.replaceAll(" ", "");

            if(sidoName.equals(regionName)) {
                sido = sido.setRegion(valueList);
            } else if(sidoSggName.equals(regionName)) {
                sigungu = sido.setRegion(sido, valueList);
            } else if(sidoSggUmdName.equals(regionName)) {
                sido.setRegion(sigungu, valueList);
            }
        }
    }
}
