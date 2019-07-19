package com.example.realpilot.externalApiModel.kweatherDay7;

import lombok.Data;

@Data
public class Day {
    private String tm;
    private Integer tfh;
    private String tmyo;
    private String icon;
    private String wtext;
    private Integer mintemp;
    private Integer maxtemp;
    private Integer rainp;
}
