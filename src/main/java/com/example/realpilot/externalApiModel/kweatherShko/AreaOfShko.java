package com.example.realpilot.externalApiModel.kweatherShko;

import lombok.Getter;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@Getter
@XmlRootElement(name = "area")
public class AreaOfShko {
    private String areaname1;
    private String areaname2;
    private String areaname3;
    private String areaname4;

    private DayOfShko day0;
    private DayOfShko day1;
    private DayOfShko day2;
    private DayOfShko day3;
    private DayOfShko day4;
    private DayOfShko day5;
    private DayOfShko day6;
    private DayOfShko day7;
    private List<DayOfShko> dayList =  new ArrayList<>();

    @XmlAttribute
    private String code;
    @XmlAttribute
    private String reg_id;

    public void setDay0 (DayOfShko day0) {
        this.day0 = day0;
        dayList.add(day0);
    }
    public DayOfShko getDay0() {
        return day0;
    }

    public void setDay1 (DayOfShko day1) {
        this.day1 = day1;
        dayList.add(day1);
    }
    public DayOfShko getDay1() {
        return day1;
    }

    public void setDay2 (DayOfShko day2) {
        this.day2 = day2;
        dayList.add(day2);
    }
    public DayOfShko getDay2() {
        return day2;
    }

    public void setDay3 (DayOfShko day3) {
        this.day3 = day3;
        dayList.add(day3);
    }
    public DayOfShko getDay3() {
        return day3;
    }

    public void setDay4(DayOfShko day4) {
        this.day4 = day4;
        dayList.add(day4);
    }
    public DayOfShko getDay4() {
        return day4;
    }

    public void setDay5 (DayOfShko day5) {
        this.day5 = day5;
        dayList.add(day5);
    }
    public DayOfShko getDay5() {
        return day5;
    }

    public void setDay6 (DayOfShko day6) {
        this.day6 = day6;
        dayList.add(day6);
    }
    public DayOfShko getDay6() {
        return day6;
    }

    public void setDay7 (DayOfShko day7) {
        this.day7 = day7;
        dayList.add(day7);
    }
    public DayOfShko getDay7() {
        return day7;
    }
}