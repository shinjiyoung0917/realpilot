package com.example.realpilot.service;

import com.example.realpilot.dao.RegionDao;
import com.example.realpilot.externalApiModel.tmCoordinate.OpenModel;
import com.example.realpilot.externalApiModel.tmCoordinate.TmCoordinate;
import com.example.realpilot.utilAndConfig.ExcelFileName;
import com.example.realpilot.utilAndConfig.RegionListIndex;
import com.example.realpilot.utilAndConfig.SidoList;
import com.example.realpilot.utilAndConfig.WxMappingJackson2HttpMessageConverter;
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

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.*;

@Service
public class RegionService<T> {
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

    private Integer SIDO_CELL_INDEX = 1;
    private Integer SIGUNGU_CELL_INDEX = 2;
    private Integer EUBMYEONDONG_CELL_INDEX = 3;

    private Map<String, List<T>> regionDataMap = new LinkedHashMap<>();
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

        for(String regionName: regionDataMap.keySet()) {
            log.info("[Service] doForGridFile - " + regionName + " / " + regionDataMap.get(regionName));
        }
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
                List<T> valueList = new ArrayList();

                for(int columnIndex = 0; columnIndex <= numberOfCells ; ++columnIndex) {
                    XSSFCell cell = sheet.getRow(rowIndex).getCell((short)columnIndex);
                    if(cell == null) {
                        continue;
                    } else {
                        keyString = addAddressCodeDataToMap(cell, keyString, valueList, columnIndex);
                    }
                }

                if (checkForValidData(keyString)) {
                    regionDataMap.put(keyString, valueList);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String addAddressCodeDataToMap(XSSFCell cell, String keyString, List<T> valueList, int columnIndex) {
        switch (cell.getCellType()) {
            case FORMULA:
                valueList.add((T)cell.getCellFormula());
                break;
            case NUMERIC:
                valueList.add((T)(Object)cell.getNumericCellValue());
                break;
            case STRING:
                if(columnIndex == SIDO_CELL_INDEX || columnIndex == SIGUNGU_CELL_INDEX || columnIndex == EUBMYEONDONG_CELL_INDEX) {
                    String stringCellValue = cell.getStringCellValue().replaceAll(" ", "");
                    keyString += stringCellValue;
                    valueList.add((T)cell.getStringCellValue());
                } else {
                    valueList.add((T)cell.getStringCellValue());
                }
                break;
            case BLANK:
                valueList.add((T)""); //cell.getBooleanCellValue()
                break;
            }
        return keyString;
    }

    private void parseGridFile(XSSFSheet sheet, int numberOfRows) {
        log.info("[Service] parseGridFile 로그 - 진입");

        for(int rowIndex = 1 ; rowIndex < numberOfRows ; ++rowIndex) {
            XSSFRow row = sheet.getRow(rowIndex);

            if(row != null) {
                int numberOfCells = row.getPhysicalNumberOfCells();

                String keyString = "";
                List<T> extraValueList = new ArrayList();

                for(int columnIndex = 0; columnIndex <= numberOfCells ; ++columnIndex) {
                    XSSFCell cell = sheet.getRow(rowIndex).getCell((short)columnIndex);
                    if(cell == null) {
                        continue;
                    } else {
                        keyString = addGridDataToMap(cell, keyString, extraValueList, columnIndex);
                    }
                }

                regionDataMap.get(keyString).addAll(extraValueList);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String addGridDataToMap(XSSFCell cell, String keyString, List<T> valueList, int columnIndex) {
        switch (cell.getCellType()) {
            case FORMULA:
                valueList.add((T)cell.getCellFormula());
                break;
            case NUMERIC:
                valueList.add((T)(Object)cell.getNumericCellValue());
                break;
            case STRING:
                if(columnIndex == SIDO_CELL_INDEX - 1 || columnIndex == SIGUNGU_CELL_INDEX - 1 || columnIndex == EUBMYEONDONG_CELL_INDEX - 1) {
                    String stringCellValue = cell.getStringCellValue().replaceAll(" ", "");
                    keyString += stringCellValue;
                } else {
                    valueList.add((T)cell.getStringCellValue());
                }
                break;
            case BLANK:
                valueList.add((T)"");
                break;
        }
        return keyString;
    }

    private void addGridDataToSet() {
        for(Map.Entry<String, List<T>> entry : regionDataMap.entrySet()) {
            if(entry.getValue().size() == RegionListIndex.LIST_SIZE_INCLUDE_GRID.getListIndex()) {
                // List<T>로 하는게 맞는거겠지,,
                List<Integer> grid = new ArrayList<>();
                grid.add((entry.getValue().get(RegionListIndex.GRID_X_INDEX.getListIndex()));
                grid.add(entry.getValue().get(RegionListIndex.GRID_X_INDEX.getListIndex()));
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
        // List를 돌면서 각 지역별로 TM좌표 저장
        for(TmCoordinate tm : tmCoordinateList) {
            String fullRegionName = tm.getSidoName() + tm.getSggName() + tm.getUmdName();
            fullRegionName = fullRegionName.replaceAll(" ", "");

            Optional<List<T>> optionalValueList = Optional.ofNullable(regionDataMap.get(fullRegionName));
            if(optionalValueList.isPresent()) {
                optionalValueList.get().add((T)(Object)tm.getTmX());
                optionalValueList.get().add((T)(Object)tm.getTmY());
            }
        }
    }
}
