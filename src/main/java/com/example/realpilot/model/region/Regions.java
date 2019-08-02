package com.example.realpilot.model.region;

import com.example.realpilot.externalApiModel.nearbyMeasureStationList.NearbyMeasureStationList;
import com.example.realpilot.externalApiModel.tmCoordinate.TmCoordinate;
import com.example.realpilot.model.airPollution.AirPollutionDetail;
import com.example.realpilot.model.weather.AmWeather;
import com.example.realpilot.model.weather.DailyWeather;
import com.example.realpilot.model.weather.HourlyWeather;
import com.example.realpilot.model.weather.PmWeather;
import com.example.realpilot.utilAndConfig.AddressCodeFileIndex;
import com.example.realpilot.utilAndConfig.GridFileIndex;
import com.example.realpilot.utilAndConfig.RegionUnit;
import lombok.Data;
import org.apache.poi.xssf.usermodel.XSSFCell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class Regions {
    private String uid;

    private String hCode;
    private String sidoName;
    private String sggName;
    private String umdName;
    private String createdDate;
    private Integer gridX;
    private Integer gridY;
    private Double tmX;
    private Double tmY;

    private String measureStationName;
    private String measureStationAddr;

    private List<Sido> sidos = new ArrayList<>(); // 연속적으로 한번에 삽입할 때 사용
    private List<Sigungu> sigungus = new ArrayList<>();
    private List<Eubmyeondong> eubmyeondongs = new ArrayList<>();
    private List<HourlyWeather> hourlyWeathers = new ArrayList<>();
    private List<DailyWeather> dailyWeathers = new ArrayList<>();
    private List<AmWeather> amWeathers = new ArrayList<>();
    private List<PmWeather> pmWeathers = new ArrayList<>();
    private List<AirPollutionDetail> airPollutionDetails = new ArrayList<>();

    public void setRegion(Regions region) {
        this.uid = region.getUid();
        this.hCode = region.getHCode();
        this.sidoName = region.getSidoName();
        this.sggName = region.getSggName();
        this.umdName = region.getUmdName();
        this.createdDate = region.getCreatedDate();
        this.gridX = region.getGridX();
        this.gridY = region.getGridY();
        this.tmX = region.getTmX();
        this.tmY = region.getTmY();
    }

    public void setRegionUidAndName(Regions region,  RegionUnit regionUnit) {
        switch (regionUnit) {
            case SIDO:
                this.uid = region.getUid();
                this.sidoName = region.getSidoName();
                break;
            case SIDO_SGG:
                List<Sigungu> sigunguList1 = region.getSigungus();
                if(Optional.ofNullable(sigunguList1).isPresent() && !sigunguList1.isEmpty()) {
                    this.uid = sigunguList1.get(0).getUid();
                    this.sggName = sigunguList1.get(0).getSggName();
                    this.sigungus = sigunguList1;
                }
                break;
            case SIDO_SGG_UMD:
                List<Sigungu> sigunguList2 = region.getSigungus();
                if(Optional.ofNullable(sigunguList2).isPresent() && !sigunguList2.isEmpty()) {
                    List<Eubmyeondong> eubmyeondongs = sigunguList2.get(0).getEubmyeondongs();
                    if(Optional.ofNullable(eubmyeondongs).isPresent() && !eubmyeondongs.isEmpty()) {
                        this.uid = eubmyeondongs.get(0).getUid();
                        this.umdName = eubmyeondongs.get(0).getUmdName();
                        this.sigungus = sigunguList2;
                    }
                }
                break;
            case SIDO_UMD:
                List<Eubmyeondong> eubmyeondongList = region.getEubmyeondongs();
                if(Optional.ofNullable(eubmyeondongList).isPresent() && !eubmyeondongList.isEmpty()) {
                    this.uid = eubmyeondongList.get(0).getUid();
                    this.umdName = eubmyeondongList.get(0).getUmdName();
                    this.eubmyeondongs = eubmyeondongList;
                }
                break;
        }
    }

    public String setRegionByAddressCode(XSSFCell cell, int columnIndex, String keyString) {
        if(columnIndex == AddressCodeFileIndex.ADDRESS_CODE_INDEX.getIndex()) {
            this.hCode = parseByDataType(cell);
        } else if(columnIndex == AddressCodeFileIndex.SIDO_NAME_INDEX.getIndex()) {
            String stringCellValue = cell.getStringCellValue().replaceAll(" ", "");
            keyString += stringCellValue;
            this.sidoName = parseByDataType(cell);
        } else if(columnIndex == AddressCodeFileIndex.SIGUNGU_NAME_INDEX.getIndex()) {
            String stringCellValue = cell.getStringCellValue().replaceAll(" ", "");
            keyString += stringCellValue;
            this.sggName = parseByDataType(cell);
        } else if(columnIndex == AddressCodeFileIndex.EUBMYEONDONG_NAME_INDEX.getIndex()) {
            String stringCellValue = cell.getStringCellValue().replaceAll(" ", "");
            keyString += stringCellValue;
            this.umdName = parseByDataType(cell);
        } else if(columnIndex == AddressCodeFileIndex.CREATED_DATE_INDEX.getIndex()) {
            this.createdDate = parseByDataType(cell);
        }

        return keyString;
    }

    public String setRegionByGrid(XSSFCell cell, int columnIndex, String keyString, Optional<Regions> originalRegion) {
        if(columnIndex == GridFileIndex.SIDO_NAME_INDEX.getIndex()) {
            String stringCellValue = cell.getStringCellValue().replaceAll(" ", "");
            keyString += stringCellValue;
        } else if(columnIndex == GridFileIndex.SIGUNGU_NAME_INDEX.getIndex()) {
            String stringCellValue = cell.getStringCellValue().replaceAll(" ", "");
            // '세종특별자치시'의 경우 시도 이름=시군구 이름이기 때문에 생략하기 위함
            if(!keyString.equals(stringCellValue)) {
                keyString += stringCellValue;
            }
        } else if(columnIndex == GridFileIndex.EUBMYEONDONG_NAME_INDEX.getIndex()) {
            String stringCellValue = cell.getStringCellValue().replaceAll(" ", "");
            keyString += stringCellValue;
        } else if(columnIndex == GridFileIndex.GRID_X_INDEX.getIndex()) {
            originalRegion.ifPresent(regionData -> regionData.setGridX(Integer.parseInt(parseByDataType(cell))));
        } else if(columnIndex == GridFileIndex.GRID_Y_INDEX.getIndex()) {
            originalRegion.ifPresent(regionData -> regionData.setGridY(Integer.parseInt(parseByDataType(cell))));
        }

        return keyString;
    }

    private String parseByDataType(XSSFCell cell) {
        String value = "";
        switch (cell.getCellType()) {
            case FORMULA:
                value = cell.getCellFormula();
                break;
            case NUMERIC:
                Double doubleValue = cell.getNumericCellValue();
                Integer integerValue = doubleValue.intValue();
                value = integerValue.toString();
                break;
            case STRING:
                value= cell.getStringCellValue();
                break;
            case BLANK:
                break;
        }
        return value;
    }

    public void setEubmyeondongByMeasureSation(NearbyMeasureStationList measureStation) {
        this.setMeasureStationName(measureStation.getStationName());
        this.setMeasureStationAddr(measureStation.getAddr());
    }
}
