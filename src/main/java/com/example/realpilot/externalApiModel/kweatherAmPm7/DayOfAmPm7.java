package com.example.realpilot.externalApiModel.kweatherAmPm7;

import lombok.Data;

@Data
public class DayOfAmPm7 {
    private String tm;
    private Integer tfh;
    private String tmyo;
    private String amicon;
    private String pmicon;
    private String amwtext;
    private String pmwtext;
    private Integer ammintemp;
    private Integer pmmintemp;
    private Integer ammaxtemp;
    private Integer pmmaxtemp;
    private Integer amrainp;
    private Integer pmrainp;
}
