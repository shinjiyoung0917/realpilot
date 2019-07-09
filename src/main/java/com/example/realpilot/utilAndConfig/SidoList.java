package com.example.realpilot.utilAndConfig;

import lombok.Getter;
import lombok.Setter;

public enum SidoList {
    SEOUL("서울"), BUSAN("부산"),
    DAEGU("대구"), INCHEON("인천"),
    GWANGJU("광주"), DAEJEON("대전"),
    ULSAN("울산"), SEJONG("세종"),
    GYEONGGI("경기"), GANGWON("강원"),
    CHUNGCHEONG("충청"),
    JEOLLA("전라"),
    GYEONGSANG("경상"),
    JEJU("제주");

    @Getter
    @Setter
    private String sidoName;

    SidoList(String sidoName) {
        this.sidoName = sidoName;
    }
}
