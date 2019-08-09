package com.example.realpilot.utilAndConfig;

import lombok.Getter;
import lombok.Setter;

public enum CountryList {
    KOREA("대한민국"),
    HONGKONG("홍콩"),
    LOSANGELES("로스엔젤레스"),
    TORONTO("토론토"),
    CAPETOWN("케이프타운"),
    PARIS("파리"),
    CAIRO("카이로"),
    NAIROBI("나이로비"),
    NEWDELHI("뉴델리"),
    NEWYORK("뉴욕"),
    DUBAI("두바이"),
    TOKYO("도쿄"),
    ROME("로마"),
    LONDON("런던"),
    MOSCOW("모스크바"),
    RIODEJANEIRO("리우데자네이루"),
    MEXICOCITY("멕시코시티"),
    BANGKOK("방콕"),
    BEIJING("베이징"),
    BERLIN("베를린"),
    VANCOUVER("벤쿠버"),
    SYDNEY("시드니"),
    SINGAPORE("싱가포르"),
    AUCKLAND("오클랜드"),
    WASHINGTONDC("워싱턴DC");


    @Getter
    @Setter
    private String countryName;

    CountryList(String countryName) {
        this.countryName = countryName;
    }
}
