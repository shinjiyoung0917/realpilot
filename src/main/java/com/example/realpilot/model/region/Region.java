package com.example.realpilot.model.region;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Region {
    private List<Sido> sidos = new ArrayList<>();
}
