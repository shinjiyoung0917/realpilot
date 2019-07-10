package com.example.realpilot.model.region;

import com.example.realpilot.utilAndConfig.RegionListIndex;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Sigungu<T> {
    private String uid;
    private T hCode;
    private T sggName;
    private T createdDate;
    private T gridX;
    private T gridY;
    private List<Eubmyeondong> eubmyeondongs = new ArrayList<>();

    public Sigungu setRegion(Sido<T> sidoObject, List<T> valueList) {
        this.hCode = valueList.get(RegionListIndex.ADDRESS_CODE_INDEX.getListIndex());
        this.sggName = valueList.get(RegionListIndex.SIGUNGU_NAME_INDEX.getListIndex());
        this.createdDate = valueList.get(RegionListIndex.CREATED_DATE_INDEX.getListIndex());

        if(valueList.size() == RegionListIndex.LIST_SIZE_INCLUDE_GRID.getListIndex()) {
            this.gridX = valueList.get(RegionListIndex.GRID_X_INDEX.getListIndex());
            this.gridY = valueList.get(RegionListIndex.GRID_Y_INDEX.getListIndex());
        }

        sidoObject.getSigungus().add(this);

        return this;
    }

}
