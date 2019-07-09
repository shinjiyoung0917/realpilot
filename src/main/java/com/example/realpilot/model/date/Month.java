package com.example.realpilot.model.date;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Month implements Serializable {
    private String uid;
    private Integer month;
    private List<Day> days = new ArrayList<>();
}
