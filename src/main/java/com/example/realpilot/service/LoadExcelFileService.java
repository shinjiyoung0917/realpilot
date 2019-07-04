package com.example.realpilot.service;

import com.example.realpilot.repository.EubmyeondongRepository;
import com.example.realpilot.repository.SidoRepository;
import com.example.realpilot.repository.SigunguRepository;
import com.example.realpilot.utilAndConfig.ExcelFileName;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Service
public class LoadExcelFileService<T> {
    private static final Logger log = LoggerFactory.getLogger(LoadExcelFileService.class);

    @Autowired
    private SidoRepository sidoRepo;
    @Autowired
    private SigunguRepository sigunguRepo;
    @Autowired
    private EubmyeondongRepository eubmyeondongRepo;
    @Autowired
    private LoadTmCoordinateService loadTmCoordinateService;


    @Autowired
    private LoadWeatherWarningService loadWeatherWarningService;


    @Autowired
    protected SessionFactory sessionFactory;

    @Autowired
    public LoadExcelFileService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Value("${addressCode.file.path}")
    private String addressCodeFilePath;
    @Value("${grid.file.path}")
    private String gridFilePath;

    private  Integer HCODE_CELL_INDEX = 0;
    private Integer SIDO_CELL_INDEX = 1;
    private Integer SIGUNGU_CELL_INDEX = 2;
    private Integer EUBMYEONDONG_CELL_INDEX = 3;
    private Integer CREATEDDATE_CELL_INDEX = 4;

    public Map<String, List> regionDataMap = new HashMap<>();


    @PostConstruct
    private void doForAddressCodeFile() throws IOException {
        log.info("readAddressCodeExcelFile 로그 - 진입");

        Long countOfRegionNodes = sidoRepo.findCountOfRegionNodes();
        log.info("createRegionNode 로그  - 지역데이터 노드 수 : " + countOfRegionNodes);

        //if(countOfRegionNodes == 0) {
            FileInputStream fis = new FileInputStream(addressCodeFilePath);
            readAnyExcelFile(fis, ExcelFileName.ADDRESS_CODE);
        //}

        doForGridFile();
        //loadTmCoordinateService.callTmCoordinateApi();
        loadWeatherWarningService.callWeatherWarningApi();

        for(String regionName: regionDataMap.keySet()) {
            log.info(regionName + " / " + regionDataMap.get(regionName));
        }
    }

    private void doForGridFile() throws IOException {
        log.info("readGridExcelFile 로그 - 진입");

        FileInputStream fis = new FileInputStream(gridFilePath);
        readAnyExcelFile(fis, ExcelFileName.GRID);
    }

    private void readAnyExcelFile(FileInputStream fis, ExcelFileName fileName) throws IOException {
        log.info("readAnyExcelFile 로그 - 진입");

        XSSFWorkbook workbook = new XSSFWorkbook(fis);

        XSSFSheet sheet = workbook.getSheetAt(0);
        int numberOfRows = sheet.getPhysicalNumberOfRows();

        if(fileName == ExcelFileName.ADDRESS_CODE) {
            parseAddressCodeFile(sheet, numberOfRows);
        } else if(fileName == ExcelFileName.GRID) {
            parseGridFile(sheet, numberOfRows);
        }

        // DB에 INSERT
        //bulkInsertForAddressCodeData(regionDataMap, numberOfRows);
    }

    private void parseAddressCodeFile(XSSFSheet sheet, int numberOfRows) {
        log.info("parseAddressCodeFile 로그 - 진입");

        for(int rowIndex = 1 ; rowIndex < numberOfRows ; ++rowIndex) {
            XSSFRow row = sheet.getRow(rowIndex);

            if(row != null) {
                int numberOfCells = row.getPhysicalNumberOfCells();

                String keyString = "";
                List valueList = new ArrayList();

                for(int columnIndex = 0; columnIndex <= numberOfCells ; ++columnIndex) {
                    XSSFCell cell = sheet.getRow(rowIndex).getCell((short)columnIndex);
                    if(cell == null) {
                        continue;
                    } else {
                        keyString = putRegionDataForAddressCodeFile(cell, keyString, valueList, columnIndex);
                    }
                }

                if (checkForValidData(keyString)) {
                    regionDataMap.put(keyString, valueList);
                }
            }
        }
    }

    private String putRegionDataForAddressCodeFile(XSSFCell cell, String keyString, List valueList, int columnIndex) {
        switch (cell.getCellType()) {
            case FORMULA:
                valueList.add(cell.getCellFormula());
                break;
            case NUMERIC:
                valueList.add(cell.getNumericCellValue());
                break;
            case STRING:
                if(columnIndex == SIDO_CELL_INDEX || columnIndex == SIGUNGU_CELL_INDEX || columnIndex == EUBMYEONDONG_CELL_INDEX) {
                    String stringCellValue = cell.getStringCellValue().replaceAll(" ", "");
                    keyString += stringCellValue;
                } else {
                    valueList.add(cell.getStringCellValue());
                }
                break;
            /*case BLANK:
                valueList.add(cell.getBooleanCellValue());
                break;
            */
        }
        return keyString;
    }

    private void parseGridFile(XSSFSheet sheet, int numberOfRows) {
        log.info("parseGridFile 로그 - 진입");

        for(int rowIndex = 1 ; rowIndex < numberOfRows ; ++rowIndex) {
            XSSFRow row = sheet.getRow(rowIndex);

            if(row != null) {
                int numberOfCells = row.getPhysicalNumberOfCells();

                String keyString = "";
                List extraValueList = new ArrayList();

                for(int columnIndex = 0; columnIndex <= numberOfCells ; ++columnIndex) {
                    XSSFCell cell = sheet.getRow(rowIndex).getCell((short)columnIndex);
                    if(cell == null) {
                        continue;
                    } else {
                        keyString = putRegionDataForGridFile(cell, keyString, extraValueList, columnIndex);
                    }
                }

                regionDataMap.get(keyString).addAll(extraValueList);
            }
        }
    }

    private String putRegionDataForGridFile(XSSFCell cell, String keyString, List valueList, int columnIndex) {
        switch (cell.getCellType()) {
            case FORMULA:
                valueList.add(cell.getCellFormula());
                break;
            case NUMERIC:
                valueList.add(cell.getNumericCellValue());
                break;
            case STRING:
                if(columnIndex == SIDO_CELL_INDEX - 1 || columnIndex == SIGUNGU_CELL_INDEX - 1 || columnIndex == EUBMYEONDONG_CELL_INDEX - 1) {
                    String stringCellValue = cell.getStringCellValue().replaceAll(" ", "");
                    keyString += stringCellValue;
                } else {
                    valueList.add(cell.getStringCellValue());
                }
                break;
        }
        return keyString;
    }

    private boolean checkForValidData(String keyString) {
        if(keyString.contains("출장")) {
            log.info("checkForValidData - 'oo출장' 문자열 포함되어 있음");
            return false;
        }
        return true;
    }



    /*@Transactional
    public void bulkInsertForAddressCodeData(Map regionDataMap, Integer numberOfRows) {
        Eubmyeondong eubmyeondong = new Eubmyeondong();
        Sigungu sigungu = new Sigungu();
        Sido sido = new Sido();

        // TODO: bulk or batch insert로 수정
        for(int i = 1 ; i < numberOfRows ; ++i) {
            List regionDatatList = regionDataMap.get(i);

            if(regionDatatList.isEmpty()) {
                break;
            }

            Session session = sessionFactory.openSession();

            if(regionDatatList.get(EUBMYEONDONG_CELL_INDEX) instanceof String) {
                eubmyeondong.setHCode(Long.parseLong((String)regionDatatList.get(HCODE_CELL_INDEX)));
                eubmyeondong.setCreatedDate(Integer.parseInt((String)regionDatatList.get(CREATEDDATE_CELL_INDEX)));
                eubmyeondong.setEubmyeondongName((String)regionDatatList.get(EUBMYEONDONG_CELL_INDEX));
                sigungu.addEubmyeondong(eubmyeondong);
            }

            if(regionDatatList.get(SIGUNGU_CELL_INDEX) instanceof String) {
                if(regionDatatList.get(EUBMYEONDONG_CELL_INDEX) instanceof Boolean) {
                    sigungu = new Sigungu();    // 시군구-읍면동 간의 relationship을 끊어줘야하기 때문에 (관련없는 이전 시군구의 마지막 읍면동 데이터를 연결하기 때문) 객체 새로 생성
                    sido.removeSigungu();       // 이미 저장된 시군구가 새로 생성된 시군구의 읍면동 데이터를 다시 연결하기 때문에 이미 저장된 시군구 HashSet에서 삭제

                    sigungu.setHCode(Long.parseLong((String)regionDatatList.get(HCODE_CELL_INDEX)));
                    sigungu.setCreatedDate(Integer.parseInt((String)regionDatatList.get(CREATEDDATE_CELL_INDEX)));
                }
                sigungu.setSigunguName((String)regionDatatList.get(SIGUNGU_CELL_INDEX));
                sido.addSigungu(sigungu);
            } else if(regionDatatList.get(SIGUNGU_CELL_INDEX) instanceof Boolean) {
                sido = new Sido();              // 시도-시군구 간의 relationship을 끊어줘야하기 때문에 (관련없는 이전 시도의 마지막 시군구 데이터를 연결하기 때문) 객체 새로 생성

                sido.setHCode(Long.parseLong((String)regionDatatList.get(HCODE_CELL_INDEX)));
                sido.setCreatedDate(Integer.parseInt((String)regionDatatList.get(CREATEDDATE_CELL_INDEX)));
            }
            sido.setSidoName((String)regionDatatList.get(SIDO_CELL_INDEX));
            session.save(sido);

        }

    }
    */

}
