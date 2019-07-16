package com.example.realpilot.model.date;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Month {
    private String uid;

    private Integer month;

    private List<Day> days = new ArrayList<>();

    public void setDate(int m, List<Day> dayList) {
        this.month = m;
        this.days = dayList;
    }
}
