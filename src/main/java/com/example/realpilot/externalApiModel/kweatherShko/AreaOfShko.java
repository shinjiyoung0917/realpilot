package com.example.realpilot.externalApiModel.kweatherShko;

import lombok.Getter;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@XmlRootElement(name = "area")
public class AreaOfShko {
    private String areaname1;
    private String areaname2;
    private String areaname3;
    private String areaname4;

    private String tm;
    private Integer tfh;
    private String tmyo;
    private String wtext;

    private Float temp;
    private String icon40a;

    @XmlAttribute
    private String code;
    @XmlAttribute
    private String reg_id;
}