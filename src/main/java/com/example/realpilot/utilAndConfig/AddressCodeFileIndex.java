package com.example.realpilot.utilAndConfig;

import lombok.Getter;
import lombok.Setter;

public enum  AddressCodeFileIndex {
    ADDRESS_CODE_INDEX(0),
    SIDO_NAME_INDEX(1),
    SIGUNGU_NAME_INDEX(2),
    EUBMYEONDONG_NAME_INDEX(3),
    CREATED_DATE_INDEX(4),
    GRID_X_INDEX(5),
    GRID_Y_INDEX(6);

    @Getter
    @Setter
    private final int index;

    AddressCodeFileIndex(int index) {
        this.index = index;
    }
}
