package com.example.realpilot.model.region;

import com.example.realpilot.utilAndConfig.RegionListIndex;
import lombok.Data;

import java.util.List;

@Data
public class Eubmyeondong<T> {
    private String uid;
    private T hCode;
    private T umdName;
    private T createdDate;
    private T gridX;
    private T gridY;
    private T tmX;
    private T tmY;

    public Eubmyeondong setRegion(Sigungu<T> sggObject, List<T> valueList) {
        this.hCode = valueList.get(RegionListIndex.ADDRESS_CODE_INDEX.getListIndex());
        this.umdName = valueList.get(RegionListIndex.EUBMYEONDONG_NAME_INDEX.getListIndex());
        this.createdDate = valueList.get(RegionListIndex.CREATED_DATE_INDEX.getListIndex());

        if(valueList.size() == RegionListIndex.LIST_SIZE_INCLUDE_GRID.getListIndex()) {
            this.gridX = valueList.get(RegionListIndex.GRID_X_INDEX.getListIndex());
            this.gridY = valueList.get(RegionListIndex.GRID_Y_INDEX.getListIndex());
        }

        if(valueList.size() == RegionListIndex.LIST_SIZE_INCLUDE_TMCOORD.getListIndex()) {
            this.tmX = valueList.get(RegionListIndex.TM_X_INDEX.getListIndex());
            this.tmY = valueList.get(RegionListIndex.TM_Y_INDEX.getListIndex());
        }

        sggObject.getEubmyeondongs().add(this);

        return this;
    }

}
