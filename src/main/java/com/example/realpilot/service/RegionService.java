package com.example.realpilot.service;

import com.example.realpilot.dao.RegionDao;
import com.example.realpilot.excelModel.RegionData;
import com.example.realpilot.externalApiModel.tmCoordinate.OpenModel;
import com.example.realpilot.externalApiModel.tmCoordinate.TmCoordinate;
import com.example.realpilot.utilAndConfig.*;
import io.dgraph.DgraphClient;
import io.dgraph.Transaction;
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
    private DgraphClient dgraphClient;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${tmCoordinate.api.url}")
    private String tmCoordinateApiUrl;
    @Value("${api.serviceKey}")
    private String serviceKey;

    @Value("${addressCode.file.path}")
    private String addressCodeFilePath;
    @Value("${grid.file.path}")
    private String gridFilePath;

    public Map<String, RegionData> regionDataMap = new LinkedHashMap<>();
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
                RegionData regionData = new RegionData();

                for(int columnIndex = 0; columnIndex <= numberOfCells ; ++columnIndex) {
                    XSSFCell cell = sheet.getRow(rowIndex).getCell((short)columnIndex);
                    if(cell == null) {
                        continue;
                    } else {
                        keyString = regionData.setRegionDataByAddressCode(cell, columnIndex, keyString);
                    }
                }

                if (checkForValidData(keyString)) {
                    regionDataMap.put(keyString, regionData);
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
                RegionData tempRegionData = new RegionData();


                for(int columnIndex = 0; columnIndex <= numberOfCells ; ++columnIndex) {
                    XSSFCell cell = sheet.getRow(rowIndex).getCell((short)columnIndex);
                    if(cell == null) {
                        continue;
                    } else {
                        Optional<RegionData> originalRegionData = Optional.ofNullable(regionDataMap.get(keyString));
                        keyString = tempRegionData.setRegionDataByGrid(cell, columnIndex, keyString, originalRegionData);
                    }
                }
            }
        }
    }

    private void addGridDataToSet() {
        for(Map.Entry<String, RegionData> entry : regionDataMap.entrySet()) {
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

    // TODO : DgraphClient dao쪽에서 선언하도록 수정
    public void addRegionNode() {
        Transaction transaction = dgraphClient.newTransaction();

        regionDao.createRegionNode(transaction, regionDataMap);
        log.info("[Service] addRegionNode 로그 - DB에 지역 데이터 삽입 완료");
    }

    public void callTmCoordinateApi() {
        restTemplate.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());

        // TODO: null말고 new로 초기화 해주기
        OpenModel openModel = null;
        for(SidoList sido : SidoList.values()) {
            log.info("[Service] callTmCoordinateApi - 시도 이름 : " + sido.getSidoName());

            URI uri = URI.create(tmCoordinateApiUrl + "?ServiceKey=" + serviceKey + "&umdName=" + sido.getSidoName() + "&numOfRows=" + 600 + "&_returnType=json");

            try {
                openModel = restTemplate.getForObject(uri, OpenModel.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            parseTmCoordinate(openModel.getList());
        }
    }

    private void parseTmCoordinate(List<TmCoordinate> tmCoordinateList) {
        for(TmCoordinate tm : tmCoordinateList) {
            String fullRegionName = tm.getSidoName() + tm.getSggName() + tm.getUmdName();
            fullRegionName = fullRegionName.replaceAll(" ", "");

            Optional<RegionData> optionalRegionData = Optional.ofNullable(regionDataMap.get(fullRegionName));
            if(optionalRegionData.isPresent()) {
                optionalRegionData.get().setRegionDataByTmCoord(tm);
            }
        }
    }

    public void printRegionData() {
        for(String regionName : regionDataMap.keySet()) {
            log.info("[Service] printRegionData - " + regionName + " / " + regionDataMap.get(regionName));
        }
    }
}
