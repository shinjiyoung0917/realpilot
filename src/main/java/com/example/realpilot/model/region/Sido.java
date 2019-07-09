package com.example.realpilot.model.region;

import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Sido<T> {
    private String uid;
    private Long hCode;
    private String sidoName;
    private Integer createdDate;
    private Integer gridX;
    private Integer gridY;
    private List<Sigungu> sigungus;

    public void addSigungu(Sigungu sigungu) {
        this.sigungus.add(sigungu);
    }
    public void removeSigungu() {
        this.sigungus.clear();
    }

    public Sido setRegion(List<T> valueList) {
        this.hCode = valueList.get(1);
        return this;
    }

    public Sigungu setRegion(Sido sidoObject, List<T> valueList) {
        Sigungu sigungu = sidoObject.getSigungus().get(1);

        return sigungu;
    }

    public Sido setRegion(Sigungu sggObject, List<T> valueList) {

        return this;
    }
}
