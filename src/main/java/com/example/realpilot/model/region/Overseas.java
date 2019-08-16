package com.example.realpilot.model.region;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Overseas {
    private String uid;

    private int countOfCountries;
    private List<Country> countries = new ArrayList<>();

    public void setOverseas(String uid, int count) {
        this.uid = uid;
        this.countOfCountries = count;
    }
}
