package com.example.realpilot.service;

import com.example.realpilot.model.Eubmyeondong;
import com.example.realpilot.model.Sido;
import com.example.realpilot.model.Sigungu;
import com.example.realpilot.repository.EubmyeondongRepository;
import com.example.realpilot.repository.SidoRepository;
import com.example.realpilot.repository.SigunguRepository;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReadExcelService {
    private static final Logger log = LoggerFactory.getLogger(ReadExcelService.class);

    private  Integer HCODE_CELL_INDEX = 0;
    private Integer SIDO_CELL_INDEX = 1;
    private Integer SIGUNGU_CELL_INDEX = 2;
    private Integer EUBMYEONDONG_CELL_INDEX = 3;
    private Integer CREATEDDATE_CELL_INDEX = 4;

    @Autowired
    private SidoRepository sidoRepo;
    @Autowired
    private SigunguRepository sigunguRepo;
    @Autowired
    private EubmyeondongRepository eubmyeondongRepo;
    @Autowired
    protected SessionFactory sessionFactory;

    @Autowired
    public ReadExcelService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

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
        Map<Integer, Object> map = new HashMap<>();
        for(rowIndex = 1 ; rowIndex < numberOfRows ; ++rowIndex) {
            XSSFRow row = sheet.getRow(rowIndex);

            if(row != null) {
                int numberOfCells = row.getPhysicalNumberOfCells();
                List cellsDataList = new ArrayList<>();

                for(columnIndex = 0; columnIndex <= numberOfCells ; ++columnIndex) {
                    XSSFCell cell = sheet.getRow(rowIndex).getCell((short)columnIndex);
                    if(cell == null) {
                        continue;
                    } else {
                        switch (cell.getCellType()) {
                            case FORMULA:
                                cellsDataList.add(cell.getCellFormula());
                                break;
                            case NUMERIC:
                                cellsDataList.add(cell.getNumericCellValue());
                                break;
                            case STRING:
                                cellsDataList.add(cell.getStringCellValue());
                                break;
                            case BLANK:
                                cellsDataList.add(cell.getBooleanCellValue());
                                break;
                            case ERROR:
                                cellsDataList.add(cell.getErrorCellValue());
                                break;
                        }
                    }
                }
                // TODO: "00출장" 데이터 블라인드하기
                map.put(rowIndex, cellsDataList);

                // grid 파일이면 매핑함수 호출?
            }
        }

        bulkInsertForAddressCodeData(map, numberOfRows);
    }

    @Transactional
    public void bulkInsertForAddressCodeData(Map map, Integer numberOfRows) {
        Eubmyeondong eubmyeondong = new Eubmyeondong();
        Sigungu sigungu = new Sigungu();
        Sido sido = new Sido();
        
        // TODO: bulk or batch insert로 수정
        for(int i = 1 ; i < numberOfRows ; ++i) {
            List regionDatatList = (List)map.get(i);

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
}
