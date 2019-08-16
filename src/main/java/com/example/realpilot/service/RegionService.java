package com.example.realpilot.service;

import com.example.realpilot.dao.RegionDao;
import com.example.realpilot.exceptionList.ApiCallException;
import com.example.realpilot.exceptionList.ExcelFileIOException;
import com.example.realpilot.exceptionList.ExcelFileNotFoundException;
import com.example.realpilot.externalApiModel.tmCoordinate.TmCoordinateTopModel;
import com.example.realpilot.externalApiModel.tmCoordinate.TmCoordinate;
import com.example.realpilot.model.region.*;
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

import java.io.*;
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

    public Map<String, Regions> koreaRegionMap = new LinkedHashMap<>();

    public void loadRegionData() {
        doForAddressCodeFile();
        doForGridFile();
    }

    private void doForAddressCodeFile() {
        log.info("[Service] doForAddressCodeFile 로그 - 진입");

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(addressCodeFilePath);
            try {
                readAnyExcelFile(fis, ExcelFileName.ADDRESS_CODE);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ExcelFileIOException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new ExcelFileNotFoundException();
        } finally {
            try {
                Objects.requireNonNull(fis).close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new ExcelFileIOException();
            }
        }
    }

    private void doForGridFile() {
        log.info("[Service] doForGridFile 로그 - 진입");

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(gridFilePath);
            try {
                readAnyExcelFile(fis, ExcelFileName.GRID);
            } catch(IOException e) {
                e.printStackTrace();
                throw new ExcelFileIOException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new ExcelFileNotFoundException();
        } finally {
            try {
                Objects.requireNonNull(fis).close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new ExcelFileIOException();
            }
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

            if(Optional.ofNullable(row).isPresent()) {
                int numberOfCells = row.getPhysicalNumberOfCells();

                String keyString = "";
                Regions region = new Regions();

                for(int columnIndex = 0; columnIndex < numberOfCells ; ++columnIndex) {
                    XSSFCell cell = sheet.getRow(rowIndex).getCell((short)columnIndex);

                    if(!Optional.ofNullable(cell).isPresent()) {
                        continue;
                    } else {
                        keyString = region.setRegionByAddressCode(cell, columnIndex, keyString);
                    }
                }

                if (checkForValidData(keyString)) {
                    koreaRegionMap.put(keyString, region);
                }
            }
        }
    }

    private void parseGridFile(XSSFSheet sheet, int numberOfRows) {
        log.info("[Service] parseGridFile 로그 - 진입");

        for(int rowIndex = 1 ; rowIndex < numberOfRows ; ++rowIndex) {
            XSSFRow row = sheet.getRow(rowIndex);

            if(Optional.ofNullable(row).isPresent()) {
                int numberOfCells = row.getPhysicalNumberOfCells();

                String keyString = "";
                Regions tempRegion = new Regions();

                for(int columnIndex = 0; columnIndex < numberOfCells ; ++columnIndex) {
                    XSSFCell cell = sheet.getRow(rowIndex).getCell((short)columnIndex);

                    if(!Optional.ofNullable(cell).isPresent()) {
                        continue;
                    } else {
                        Optional<Regions> originalRegion = Optional.ofNullable(koreaRegionMap.get(keyString));
                        keyString = tempRegion.setRegionByGrid(cell, columnIndex, keyString, originalRegion);
                    }
                }
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

    public void addKoreaRegionNode() {
        regionDao.createRegionNode(koreaRegionMap);
        log.info("[Service] addRegionNode 로그 - DB에 지역 데이터 삽입 완료");
    }

    public void printKoreaRegionData() {
        for(String regionName : koreaRegionMap.keySet()) {
            log.info("[Service] printRegionData - " + regionName + " / " + koreaRegionMap.get(regionName));
        }
    }

    public void addWorldRegionNode() {
        int count = 0;
        Overseas overseas;
        Optional<Overseas> optionalOverseas = regionDao.getOverseasNode();
        if(optionalOverseas.isPresent()) {
            overseas = optionalOverseas.get();
        } else {
            overseas = new Overseas();
        }

        for(CountryList oneCountry : CountryList.values()) {
            if(oneCountry.getCountryName().equals(CountryList.KOREA.getCountryName())) {
                continue;
            }
            Optional<Country> optionalCountry = regionDao.getCountryNodeWithName(oneCountry.getCountryName());
            String countryUid = null;
            if(optionalCountry.isPresent() && Optional.ofNullable(optionalCountry.get().getUid()).isPresent()) {
                countryUid = optionalCountry.get().getUid();
            }
            Country country = new Country();
            country.setCountry(countryUid, oneCountry);

            overseas.getCountries().add(country);
            ++count;
            log.info("[Service] addWorldRegionNode - " + country.getCountryName());
        }
        overseas.setOverseas(overseas.getUid(), count);
        regionDao.updateRegionNode(overseas);
        log.info("[Service] addWorldRegionNode - 모든 노드 삽입 완료");
    }

    public void callTmCoordinateApi() {
        TmCoordinateTopModel tmCoordinateTopModel = new TmCoordinateTopModel();

        for(SidoList sido : SidoList.values()) {
            log.info("[Service] callTmCoordinateApi - 시도 이름 : " + sido.getSidoName());

            URI uri = URI.create(tmCoordinateApiUrl + "?ServiceKey=" + serviceKey + "&umdName=" + sido.getSidoName() + "&numOfRows=" + 600 + "&_returnType=json");

            try {
                tmCoordinateTopModel = restTemplate.getForObject(uri, TmCoordinateTopModel.class);
            } catch (Exception e) {
                e.printStackTrace();
                throw new ApiCallException();
            }

            // TODO: 익셉션 핸들링
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for(TmCoordinate tmCoordinate : tmCoordinateTopModel.getList()) {
                Optional<Regions> optionalRegion = regionDao.getRegionNodeWithName(tmCoordinate.getSidoName(), tmCoordinate.getSggName(), tmCoordinate.getUmdName());
                Regions region = new Regions();

                if(optionalRegion.isPresent()) {
                    region = optionalRegion.get();
                    List<Sigungu> sigunguList = region.getSigungus();
                    if(Optional.ofNullable(sigunguList).isPresent() && !sigunguList.isEmpty()) {
                        List<Eubmyeondong> eubmyeondongList = sigunguList.get(0).getEubmyeondongs();
                        if(Optional.ofNullable(eubmyeondongList).isPresent() && !eubmyeondongList.isEmpty()) {
                            eubmyeondongList.get(0).setEubmyeondongByTmCoord(tmCoordinate);
                            region.setRegion(eubmyeondongList.get(0));
                        }
                    }
                }

                regionDao.updateRegionNode(region);
            }
        }
    }
}
