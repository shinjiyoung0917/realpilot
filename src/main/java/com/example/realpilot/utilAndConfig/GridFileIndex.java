package com.example.realpilot.utilAndConfig;

import lombok.Getter;
import lombok.Setter;

public enum GridFileIndex {
    SIDO_NAME_INDEX(0),
    SIGUNGU_NAME_INDEX(1),
    EUBMYEONDONG_NAME_INDEX(2),
    GRID_X_INDEX(3),
    GRID_Y_INDEX(4);

    @Getter
    @Setter
    private final int index;

    GridFileIndex(int index) {
        this.index = index;
    }
}
