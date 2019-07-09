package com.example.realpilot.model.date;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Day implements Serializable {
    private String uid;
    private Integer day;
    private List<Hour> hours = new ArrayList<>();
}
