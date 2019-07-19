package com.example.realpilot.externalApiModel.kweatherDay7;

import lombok.Data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "area")
public class Area {
    private String areaname1;
    private String areaname2;
    private String areaname3;
    private String areaname4;
    // TODO: list로 관리할지 말지
    private Day day0;
    private Day day1;
    private Day day2;
    private Day day3;
    private Day day4;
    private Day day5;
    private Day day6;
    private Day day7;

    @XmlAttribute
    private String code;
    @XmlAttribute
    private String reg_id;
}
