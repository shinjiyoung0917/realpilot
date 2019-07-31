package com.example.realpilot.service;

import com.example.realpilot.dao.RegionDao;
import com.example.realpilot.externalApiModel.tmCoordinate.TmCoordinateTopModel;
import com.example.realpilot.externalApiModel.tmCoordinate.TmCoordinate;
import com.example.realpilot.model.region.Eubmyeondong;
import com.example.realpilot.model.region.Regions;
import com.example.realpilot.model.region.Sigungu;
import com.example.realpilot.utilAndConfig.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.*;

@Service
public class RegionService {
    private static final Logger log = LoggerFactory.getLogger(RegionService.class);

    @Autowired
    private RegionDao regionDao;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.serviceKey}")
    private String serviceKey;
    @Value("${tmCoordinate.api.url}")
    private String tmCoordinateApiUrl;

    @Value("${addressCode.file.path}")
    private String addressCodeFilePath;
    @Value("${grid.file.path}")
    private String gridFilePath;

    public Map<String, Regions> regionMap = new LinkedHashMap<>();
    public Set<List<Integer>> gridSet = new LinkedHashSet<>();

    public void doForAddressCodeFile() throws IOException {
        log.info("[Service] readAddressCodeExcelFile 로그 - 진입");

        FileInputStream fis = new FileInputStream(addressCodeFilePath);
        readAnyExcelFile(fis, ExcelFileName.ADDRESS_CODE);
    }

    public void doForGridFile() throws IOException {
        log.info("[Service] readGridExcelFile 로그 - 진입");

        FileInputStream fis = new FileInputStream(gridFilePath);
        readAnyExcelFile(fis, ExcelFileName.GRID);

        addGridDataToSet();
    }

    private void readAnyExcelFile(FileInputStream fis, ExcelFileName fileName) throws IOException {
        log.info("[Service] readAnyExcelFile 로그 - 진입");

        XSSFWorkbook workbook = new XSSFWorkbook(fis);

        XSSFSheet sheet = workbook.getSheetAt(0);
        int numberOfRows = sheet.getPhysicalNumberOfRows();

        if(fileName == ExcelFileName.ADDRESS_CODE) {
            parseAddressCodeFile(sheet, numberOfRows);
        } else if(fileName == ExcelFileName.GRID) {
            parseGridFile(sheet, numberOfRows);
        }
    }

    private void parseAddressCodeFile(XSSFSheet sheet, int numberOfRows) {
        log.info("[Service] parseAddressCodeFile 로그 - 진입");

        for(int rowIndex = 1 ; rowIndex < numberOfRows ; ++rowIndex) {
            XSSFRow row = sheet.getRow(rowIndex);

            if(row != null) {
                int numberOfCells = row.getPhysicalNumberOfCells();

                String keyString = "";
                Regions region = new Regions();

                for(int columnIndex = 0; columnIndex < numberOfCells ; ++columnIndex) {
                    XSSFCell cell = sheet.getRow(rowIndex).getCell((short)columnIndex);

                    if(cell == null) {
                        continue;
                    } else {
                        keyString = region.setRegionByAddressCode(cell, columnIndex, keyString);
                    }
                }

                if (checkForValidData(keyString)) {
                    regionMap.put(keyString, region);
                }
            }
        }
    }

    private void parseGridFile(XSSFSheet sheet, int numberOfRows) {
        log.info("[Service] parseGridFile 로그 - 진입");

        for(int rowIndex = 1 ; rowIndex < numberOfRows ; ++rowIndex) {
            XSSFRow row = sheet.getRow(rowIndex);

            if(row != null) {
                int numberOfCells = row.getPhysicalNumberOfCells();

                String keyString = "";
                Regions tempRegion = new Regions();

                for(int columnIndex = 0; columnIndex < numberOfCells ; ++columnIndex) {
                    XSSFCell cell = sheet.getRow(rowIndex).getCell((short)columnIndex);

                    if(cell == null) {
                        continue;
                    } else {
                        Optional<Regions> originalRegion = Optional.ofNullable(regionMap.get(keyString));
                        keyString = tempRegion.setRegionByGrid(cell, columnIndex, keyString, originalRegion);
                    }
                }
            }
        }
    }

    private void addGridDataToSet() {
        for(Map.Entry<String, Regions> entry : regionMap.entrySet()) {
            Optional optinalValue = Optional.ofNullable(entry.getValue().getGridX());
            if(optinalValue.isPresent()) {
                List<Integer> grid = new ArrayList<>();
                grid.add(entry.getValue().getGridX());
                grid.add(entry.getValue().getGridY());
                gridSet.add(grid);
            }
        }

    }

    private boolean checkForValidData(String keyString) {
        if(keyString.contains("출장")) {
            log.info("[Service] checkForValidData - 'oo출장' 문자열 포함되어 있음");
            return false;
        }
        return true;
    }

    public void addRegionNode() {
        regionDao.createRegionNode(regionMap);
        log.info("[Service] addRegionNode 로그 - DB에 지역 데이터 삽입 완료");
    }

    public void callTmCoordinateApi() {
        restTemplate.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());

        TmCoordinateTopModel tmCoordinateTopModel = new TmCoordinateTopModel();

        for(SidoList sido : SidoList.values()) {
            log.info("[Service] callTmCoordinateApi - 시도 이름 : " + sido.getSidoName());

            URI uri = URI.create(tmCoordinateApiUrl + "?ServiceKey=" + serviceKey + "&umdName=" + sido.getSidoName() + "&numOfRows=" + 600 + "&_returnType=json");

            try {
                tmCoordinateTopModel = restTemplate.getForObject(uri, TmCoordinateTopModel.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for(TmCoordinate tm : tmCoordinateTopModel.getList()) {
                Optional<Regions> optionalRegion = regionDao.getRegionNodeWithName(tm.getSidoName(), tm.getSggName(), tm.getUmdName());
                Regions region = new Regions();

                if(optionalRegion.isPresent()) {
                    region = optionalRegion.get();
                    List<Sigungu> sigunguList = region.getSigungus();
                    if(Optional.ofNullable(sigunguList).isPresent() && !sigunguList.isEmpty()) {
                        List<Eubmyeondong> eubmyeondongList = sigunguList.get(0).getEubmyeondongs();
                        if(Optional.ofNullable(eubmyeondongList).isPresent() && !eubmyeondongList.isEmpty()) {
                            eubmyeondongList.get(0).setEubmyeondongByTmCoord(tm);
                            region.setRegion(eubmyeondongList.get(0));
                        }
                    }
                }

                regionDao.updateRegionNode(region);
            }
        }
    }

    public void printRegionData() {
        for(String regionName : regionMap.keySet()) {
            log.info("[Service] printRegionData - " + regionName + " / " + regionMap.get(regionName));
        }
    }
}
