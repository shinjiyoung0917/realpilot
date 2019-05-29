package com.example.realpilot.service;

import com.example.realpilot.model.Region;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReadExcelService {
    private static final Logger log = LoggerFactory.getLogger(ReadExcelService.class);

    @PostConstruct
    public void readAddressCodeExcelFile() throws IOException {
        log.info("readAddressCodeExcelFile 로그 - 진입");

        FileInputStream fis = new FileInputStream("C://Users/shinjiyoung/IdeaProjects/realpilot/excelFile/KIKcd_H.20190415.xlsx");
        readAnyExcelFile(fis);
    }

    @PostConstruct
    public void readGridExcelFile() throws IOException {
        log.info("readGridExcelFile 로그 - 진입");

        FileInputStream fis = new FileInputStream("C://Users/shinjiyoung/IdeaProjects/realpilot/excelFile/Grid_20190107.xlsx");
        //readAnyExcelFile(fis);
    }

    public void readAnyExcelFile(FileInputStream fis) throws IOException {
        log.info("readAnyExcelFile 로그 - 진입");

        XSSFWorkbook workbook = new XSSFWorkbook(fis);

        int rowIndex = 0;
        int columnIndex = 0;

        XSSFSheet sheet = workbook.getSheetAt(0);

        int numberOfRows = sheet.getPhysicalNumberOfRows();
        for(rowIndex = 1 ; rowIndex < numberOfRows ; ++rowIndex) {
            XSSFRow row = sheet.getRow(rowIndex);

            if(row != null) {
                int numberOfCells = row.getPhysicalNumberOfCells();
                List cellsDataList = new ArrayList<>();

                for(columnIndex = 0; columnIndex <= numberOfCells ; ++columnIndex) {
                    XSSFCell cell = sheet.getRow(rowIndex).getCell((short)columnIndex);
                    //String value = "";
                    if(cell == null) {
                        continue;
                    } else {
                        switch (cell.getCellType()) {
                            case FORMULA:
                                //value = cell.getCellFormula();
                                cellsDataList.add(cell.getCellFormula());
                                break;
                            case NUMERIC:
                                //value = cell.getNumericCellValue() + "";
                                cellsDataList.add(cell.getNumericCellValue());
                                break;
                            case STRING:
                                //value = cell.getStringCellValue() + "";
                                cellsDataList.add(cell.getStringCellValue());
                                break;
                            case BLANK:
                                //value = cell.getBooleanCellValue() + "";
                                cellsDataList.add(cell.getBooleanCellValue());
                                break;
                            case ERROR:
                                //value = cell.getErrorCellValue() + "";
                                cellsDataList.add(cell.getErrorCellValue());
                                break;
                        }
                    }
                }
                log.info("셀 내용 : " + cellsDataList);
                cellsDataList.clear();
                // jusocode 파일인지 grid 파일인지 구분해서 매핑함수 호출?
                //bulkInsertForAddressCodeData();
            }
        }
    }

    @Transactional
    public void bulkInsertForAddressCodeData(Long hCode, String siDo, String siGunGu, String eubMyeonDong, Integer createdDate) {
        Region region = Region.builder()
                .hCode(hCode)
                .siDo(siDo)
                .siGunGu(siGunGu)
                .eubMyeonDong(eubMyeonDong)
                .createdDate(createdDate)
                .build();


    }
}
