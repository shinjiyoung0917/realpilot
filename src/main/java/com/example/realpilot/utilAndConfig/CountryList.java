package com.example.realpilot.utilAndConfig;

import lombok.Getter;
import lombok.Setter;

public enum CountryList {
    KOREA("대한민국");

    @Getter
    @Setter
    private String countryName;

    CountryList(String countryName) {
        this.countryName = countryName;
    }
}
