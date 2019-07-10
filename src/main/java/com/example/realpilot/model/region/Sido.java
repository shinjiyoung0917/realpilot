package com.example.realpilot.model.region;

import com.example.realpilot.utilAndConfig.RegionListIndex;
import lombok.Data;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;

@Data
public class Sido<T> {
    private String uid;
    private T hCode;
    private T sidoName;
    private T createdDate;
    private T gridX;
    private T gridY;
    private List<Sigungu> sigungus = new ArrayList<>();

    public Sido setRegion(List<T> valueList) {
        this.hCode = valueList.get(RegionListIndex.ADDRESS_CODE_INDEX.getListIndex());
        this.sidoName = valueList.get(RegionListIndex.SIDO_NAME_INDEX.getListIndex());
        this.createdDate = valueList.get(RegionListIndex.CREATED_DATE_INDEX.getListIndex());

        // grid가 없는 경우도 있음
        if(valueList.size() == RegionListIndex.LIST_SIZE_INCLUDE_GRID.getListIndex()) {
            this.gridX = valueList.get(RegionListIndex.GRID_X_INDEX.getListIndex());
            this.gridY = valueList.get(RegionListIndex.GRID_Y_INDEX.getListIndex());
        }

        return this;
    }
}
