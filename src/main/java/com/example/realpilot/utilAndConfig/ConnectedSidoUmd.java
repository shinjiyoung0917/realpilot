package com.example.realpilot.utilAndConfig;

import lombok.Getter;
import lombok.Setter;

public enum ConnectedSidoUmd {
    SEJONG("세종특별자치시");

    @Getter
    @Setter
    private String sidoName;

    ConnectedSidoUmd(String sidoName) {
        this.sidoName = sidoName;
    }
}
