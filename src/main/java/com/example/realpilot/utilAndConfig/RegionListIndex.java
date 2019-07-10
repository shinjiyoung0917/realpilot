package com.example.realpilot.utilAndConfig;


import lombok.Getter;
import lombok.Setter;

public enum RegionListIndex {
    ADDRESS_CODE_INDEX(0),
    SIDO_NAME_INDEX(1),
    SIGUNGU_NAME_INDEX(2),
    EUBMYEONDONG_NAME_INDEX(3),
    CREATED_DATE_INDEX(4),
    GRID_X_INDEX(5),
    GRID_Y_INDEX(6),
    TM_X_INDEX(7),
    TM_Y_INDEX(8),
    LIST_SIZE_INCLUDE_GRID(7),
    LIST_SIZE_INCLUDE_TMCOORD(9);

    @Getter
    @Setter
    private final int listIndex;

    RegionListIndex(int listIndex) {
        this.listIndex = listIndex;
    }
}
