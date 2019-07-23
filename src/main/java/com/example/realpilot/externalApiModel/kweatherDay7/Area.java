package com.example.realpilot.externalApiModel.kweatherDay7;

import lombok.Data;
import lombok.Getter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

//@Data
@Getter
@XmlRootElement(name = "area")
public class Area {
    private String areaname1;
    private String areaname2;
    private String areaname3;
    private String areaname4;

    private Days day0;
    private Days day1;
    private Days day2;
    private Days day3;
    private Days day4;
    private Days day5;
    private Days day6;
    private Days day7;
    private List<Days> dayList =  new ArrayList<>();

    @XmlAttribute
    private String code;
    @XmlAttribute
    private String reg_id;

    public void setDay0 (Days day0) {
        this.day0 = day0;
        dayList.add(day0);
    }
    public Days getDay0() {
        return day0;
    }

    public void setDay1 (Days day1) {
        this.day1 = day1;
        dayList.add(day1);
    }
    public Days getDay1() {
        return day1;
    }

    public void setDay2 (Days day2) {
        this.day2 = day2;
        dayList.add(day2);
    }
    public Days getDay2() {
        return day2;
    }

    public void setDay3 (Days day3) {
        this.day3 = day3;
        dayList.add(day3);
    }
    public Days getDay3() {
        return day3;
    }

    public void setDay4(Days day4) {
        this.day4 = day4;
        dayList.add(day4);
    }
    public Days getDay4() {
        return day4;
    }

    public void setDay5 (Days day5) {
        this.day5 = day5;
        dayList.add(day5);
    }
    public Days getDay5() {
        return day5;
    }

    public void setDay6 (Days day6) {
        this.day6 = day6;
        dayList.add(day6);
    }
    public Days getDay6() {
        return day6;
    }

    public void setDay7 (Days day7) {
        this.day7 = day7;
        dayList.add(day7);
    }
    public Days getDay7() {
        return day7;
    }
}
