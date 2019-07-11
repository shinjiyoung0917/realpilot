package com.example.realpilot.excelModel;

import com.example.realpilot.externalApiModel.tmCoordinate.TmCoordinate;
import com.example.realpilot.utilAndConfig.AddressCodeFileIndex;
import com.example.realpilot.utilAndConfig.GridFileIndex;
import lombok.Data;
import org.apache.poi.xssf.usermodel.XSSFCell;

import java.util.Optional;

@Data
public class RegionData {
    private String hCode;
    private String sidoName;
    private String sggName;
    private String umdName;
    private String createdDate;
    private Integer gridX;
    private Integer gridY;
    private double tmX;
    private double tmY;

    public String setRegionDataByAddressCode(XSSFCell cell, int columnIndex, String keyString) {
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

    public String setRegionDataByGrid(XSSFCell cell, int columnIndex, String keyString, Optional<RegionData> originalRegionData) {
      if(columnIndex == GridFileIndex.SIDO_NAME_INDEX.getIndex()) {
            String stringCellValue = cell.getStringCellValue().replaceAll(" ", "");
            keyString += stringCellValue;
        } else if(columnIndex == GridFileIndex.SIGUNGU_NAME_INDEX.getIndex()) {
            String stringCellValue = cell.getStringCellValue().replaceAll(" ", "");
            keyString += stringCellValue;
        } else if(columnIndex == GridFileIndex.EUBMYEONDONG_NAME_INDEX.getIndex()) {
            String stringCellValue = cell.getStringCellValue().replaceAll(" ", "");
            keyString += stringCellValue;
        } else if(columnIndex == GridFileIndex.GRID_X_INDEX.getIndex()) {
          originalRegionData.ifPresent(regionData -> regionData.setGridX(Integer.parseInt(parseByDataType(cell))));
        } else if(columnIndex == GridFileIndex.GRID_Y_INDEX.getIndex()) {
          originalRegionData.ifPresent(regionData -> regionData.setGridY(Integer.parseInt(parseByDataType(cell))));
      }
        return keyString;
    }

    public void setRegionDataByTmCoord(TmCoordinate tm) {
        this.tmX = tm.getTmX();
        this.tmY = tm.getTmY();
    }

    private String parseByDataType(XSSFCell cell) {
        String value = "";
        switch (cell.getCellType()) {
            case FORMULA:
                value = cell.getCellFormula();
                break;
            case NUMERIC:
                Double doubleValue = Double.valueOf(cell.getNumericCellValue());
                Integer integerValue = Integer.valueOf(doubleValue.intValue());
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
}
