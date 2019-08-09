package com.example.realpilot.dao;

import com.example.realpilot.dgraph.DgraphOperations;
import com.example.realpilot.model.region.*;
import com.example.realpilot.utilAndConfig.CountryList;
import com.google.gson.Gson;
import io.dgraph.DgraphClient;
import io.dgraph.DgraphProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class RegionDao<T> {
    private static final Logger log = LoggerFactory.getLogger(RegionDao.class);

    @Autowired
    private DgraphClient dgraphClient;
    @Autowired
    private DgraphOperations operations;
    @Autowired
    private Gson gson = new Gson();

    public void createRegionNode(Map<String, Regions> koreaRegionMap) {
        Country korea = new Country();
        Sido prevSavedSido = new Sido();
        Sigungu prevSavedSigungu = new Sigungu();

        Optional<Country> optionalKorea = getCountryNodeWithName(CountryList.KOREA.getCountryName());
        Optional<String> koreadUid = Optional.empty();
        if(optionalKorea.isPresent()) {
            koreadUid = Optional.ofNullable(optionalKorea.get().getUid());
        }

        // TODO: model로 옮기기
        for(String regionName: koreaRegionMap.keySet()) {
            // koreaRegionMap의 list 타입의 value => 행정동코드, 생성날짜, 격자X,Y, TM X,Y
            Regions region = koreaRegionMap.get(regionName);

            Optional<Regions> optionalRegion = getRegionNodeWithName(region.getSidoName(), region.getSggName(), region.getUmdName());

            if(region.getSggName().equals("") && region.getUmdName().equals("")) {
                if(optionalRegion.isPresent()) {
                    region.setUid(optionalRegion.get().getUid());
                }
                Sido sido = new Sido();
                prevSavedSido = sido.setSido(region);
                korea.getSidos().add(prevSavedSido);
            } else if(!region.getSggName().equals("") && region.getUmdName().equals("")) {
                if(optionalRegion.isPresent()) {
                    List<Sigungu> sigunguList = optionalRegion.get().getSigungus();
                    if(Optional.ofNullable(sigunguList).isPresent() && !sigunguList.isEmpty()) {
                        region.setUid(sigunguList.get(0).getUid());
                    }
                }
                Sigungu sigungu = new Sigungu();
                prevSavedSigungu = sigungu.setSigungu(prevSavedSido, region);
            } else if(region.getSggName().equals("") && !region.getUmdName().equals("")) { // '세종특별자치시'의 경우 시군구 이름이 빈 칸이기 때문에 읍면동으로 바로 연결해주기 위함
                if(optionalRegion.isPresent()) {
                    List<Eubmyeondong> eubmyeondongList = optionalRegion.get().getEubmyeondongs();
                    if(Optional.ofNullable(eubmyeondongList).isPresent() && !eubmyeondongList.isEmpty()) {
                        region.setUid(eubmyeondongList.get(0).getUid());
                    }
                }
                Eubmyeondong eubmyeondong = new Eubmyeondong();
                eubmyeondong.setEubmyeondong(prevSavedSido, region);
            } else {
                if(optionalRegion.isPresent()) {
                    List<Sigungu> sigunguList = optionalRegion.get().getSigungus();
                    if(Optional.ofNullable(sigunguList).isPresent() && !sigunguList.isEmpty()) {
                        List<Eubmyeondong> eubmyeondongList = sigunguList.get(0).getEubmyeondongs();
                        if(Optional.ofNullable(eubmyeondongList).isPresent() && !eubmyeondongList.isEmpty()) {
                            region.setUid(eubmyeondongList.get(0).getUid());
                        }
                    }
                }
                Eubmyeondong eubmyeondong = new Eubmyeondong();
                eubmyeondong.setEubmyeondong(prevSavedSigungu, region);
            }
        }
        korea.setCountry(koreadUid.get(), CountryList.KOREA);

        operations.mutate(dgraphClient.newTransaction(), korea);
    }

    public Optional<Country> getCountryNodeWithName(String countryName) {
        String fullQueryString = "query country($countryName: string) {\n" +
                "  country(func: eq(countryName, $countryName)) {\n" +
                "    uid\n" +
                "    countryName\n" +
                "  }\n" +
                "}\n";

        Map<String, String> var  = new LinkedHashMap<>();
        var.put("$countryName", countryName);

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(fullQueryString, var);
        RegionRootQuery regionRootQuery = gson.fromJson(res.getJson().toStringUtf8(), RegionRootQuery.class);
        List<Country> countryResult =  regionRootQuery.getCountry();

        Optional<Country> result = Optional.empty();
        if(Optional.ofNullable(countryResult).isPresent() && !countryResult.isEmpty()) {
            result = Optional.of(countryResult.get(0));
        }

        return result;
    }

    public Optional<Regions> getRegionNodeWithName(String sidoName, String sggName, String umdName) {
        String fullQueryString = "query region($sidoName: string, $sggName: string, $umdName: string) {\n" +
                "  region(func: eq(sidoName, $sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    sigungus @filter(eq(sggName, $sggName)) {\n" +
                "      uid\n" +
                "      sggName\n" +
                "      eubmyeondongs @filter(eq(umdName, $umdName)) {\n" +
                "        uid\n" +
                "        umdName\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

        Map<String, String> var  = new LinkedHashMap<>();
        var.put("$sidoName", String.valueOf(sidoName));
        var.put("$sggName", String.valueOf(sggName));
        var.put("$umdName", String.valueOf(umdName));

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(fullQueryString, var);
        RegionRootQuery regionRootQuery = gson.fromJson(res.getJson().toStringUtf8(), RegionRootQuery.class);
        List<Regions> regionResult =  regionRootQuery.getRegion();

        Optional<Regions> result = Optional.empty();
        if(Optional.ofNullable(regionResult).isPresent() && !regionResult.isEmpty()) {
            result = Optional.of(regionResult.get(0));
        }

        return result;
    }

    public List<Regions> getRegionNodeWithGrid(Integer gridX, Integer gridY) {
        String fullQueryString = "query region($gridX: int, $gridY: int) {\n" +
                " region(func: eq(gridX, $gridX)) @filter(eq(gridY, $gridY)) {\n" +
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

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(fullQueryString, var);
        RegionRootQuery regionRootQuery = gson.fromJson(res.getJson().toStringUtf8(), RegionRootQuery.class);
        List<Regions> regionResult =  regionRootQuery.getRegion();

        // TODO: Optional로 변경
        return regionResult;
    }

    public Optional<List<Regions>> getRegionNodeWithMeasureStation(String stationName) {
        String fullQueryString = "query region($measureStationName: string) {\n" +
                " region(func: eq(measureStationName, $measureStationName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "    sggName\n" +
                "    umdName\n" +
                "    measureStationName\n" +
                "  }\n" +
                "}";

        Map<String, String> var  = new LinkedHashMap<>();
        var.put("$measureStationName", String.valueOf(stationName));

        DgraphProto.Response res = dgraphClient.newTransaction().queryWithVars(fullQueryString, var);
        RegionRootQuery regionRootQuery = gson.fromJson(res.getJson().toStringUtf8(), RegionRootQuery.class);
        List<Regions> regionResult =  regionRootQuery.getRegion();

        Optional<List<Regions>> result = Optional.empty();
        if(Optional.ofNullable(regionResult).isPresent() && !regionResult.isEmpty()) {
            result = Optional.of(regionResult);
        }

        return result;
    }

    public Optional<List<Regions>> getSidoNode() {
        String fullQueryString = "{\n" +
                "  region(func: has(sidoName)) {\n" +
                "    uid\n" +
                "    sidoName\n" +
                "  }\n" +
                "}";

        DgraphProto.Response res = dgraphClient.newTransaction().query(fullQueryString);
        RegionRootQuery regionRootQuery = gson.fromJson(res.getJson().toStringUtf8(), RegionRootQuery.class);
        List<Regions> regionResult =  regionRootQuery.getRegion();

        Optional<List<Regions>> result = Optional.empty();
        if(Optional.ofNullable(regionResult).isPresent() && !regionResult.isEmpty()) {
            result = Optional.of(regionResult);
        }

        return result;
    }

    public Optional<Set<Regions>> getGridList() {
        String fullQueryString = "query {\n" +
                "  grid(func: has(gridX)) @filter(has(gridY)) {\n" +
                "    gridX\n" +
                "    gridY\n" +
                "  }\n" +
                "}\n";

        DgraphProto.Response res = dgraphClient.newTransaction().query(fullQueryString);
        RegionRootQuery regionRootQuery = gson.fromJson(res.getJson().toStringUtf8(), RegionRootQuery.class);
        Set<Regions> gridResult =  regionRootQuery.getGrid();

        Optional<Set<Regions>> result = Optional.empty();
        if(Optional.ofNullable(gridResult).isPresent() && !gridResult.isEmpty()) {
            result = Optional.of(gridResult);
        }

        return result;
    }

    public Optional<Set<Regions>> getTmCoordinateList() {
        String fullQueryString = "query {\n" +
                "  tmCoordinate(func: has(tmX)) @filter(has(tmY)) {\n" +
                "    uid\n" +
                "    umdName\n" +
                "    tmX\n" +
                "    tmY\n" +
                "  }\n" +
                "}\n";

        DgraphProto.Response res = dgraphClient.newTransaction().query(fullQueryString);
        RegionRootQuery regionRootQuery = gson.fromJson(res.getJson().toStringUtf8(), RegionRootQuery.class);
        Set<Regions> tmCoordResult =  regionRootQuery.getTmCoordinate();

        Optional<Set<Regions>> result = Optional.empty();
        if(Optional.ofNullable(tmCoordResult).isPresent() && !tmCoordResult.isEmpty()) {
            result = Optional.of(tmCoordResult);
        }

        return result;
    }

    public Optional<Set<Regions>> getMeasureStationList() {
        String fullQueryString = "query {\n" +
                "  measureStationInfo(func: has(measureStationName)) {\n" +
                "    uid\n" +
                "    umdName\n" +
                "    measureStationName\n" +
                "  }\n" +
                "}\n";

        DgraphProto.Response res = dgraphClient.newTransaction().query(fullQueryString);
        RegionRootQuery regionRootQuery = gson.fromJson(res.getJson().toStringUtf8(), RegionRootQuery.class);
        Set<Regions> measureStationInfoResult =  regionRootQuery.getMeasureStationInfo();

        Optional<Set<Regions>> result = Optional.empty();
        if(Optional.ofNullable(measureStationInfoResult).isPresent() && !measureStationInfoResult.isEmpty()) {
            result = Optional.of(measureStationInfoResult);
        }

        return result;
    }

   public void updateRegionNode(T region) {
        operations.mutate(dgraphClient.newTransaction(), region);
    }
}
