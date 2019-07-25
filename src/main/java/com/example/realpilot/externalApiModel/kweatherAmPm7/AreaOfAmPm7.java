package com.example.realpilot.externalApiModel.kweatherAmPm7;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@Getter
@XmlRootElement(name = "area")
public class AreaOfAmPm7 {
    private String areaname1;
    private String areaname2;
    private String areaname3;
    private String areaname4;

    private DayOfAmPm7 day0;
    private DayOfAmPm7 day1;
    private DayOfAmPm7 day2;
    private DayOfAmPm7 day3;
    private DayOfAmPm7 day4;
    private DayOfAmPm7 day5;
    private DayOfAmPm7 day6;
    private DayOfAmPm7 day7;
    private List<DayOfAmPm7> dayList =  new ArrayList<>();

    @XmlAttribute
    private String code;
    @XmlAttribute
    private String reg_id;

    public void setDay0 (DayOfAmPm7 day0) {
        this.day0 = day0;
        dayList.add(day0);
    }
    public DayOfAmPm7 getDay0() {
        return day0;
    }

    public void setDay1 (DayOfAmPm7 day1) {
        this.day1 = day1;
        dayList.add(day1);
    }
    public DayOfAmPm7 getDay1() {
        return day1;
    }

    public void setDay2 (DayOfAmPm7 day2) {
        this.day2 = day2;
        dayList.add(day2);
    }
    public DayOfAmPm7 getDay2() {
        return day2;
    }

    public void setDay3 (DayOfAmPm7 day3) {
        this.day3 = day3;
        dayList.add(day3);
    }
    public DayOfAmPm7 getDay3() {
        return day3;
    }

    public void setDay4(DayOfAmPm7 day4) {
        this.day4 = day4;
        dayList.add(day4);
    }
    public DayOfAmPm7 getDay4() {
        return day4;
    }

    public void setDay5 (DayOfAmPm7 day5) {
        this.day5 = day5;
        dayList.add(day5);
    }
    public DayOfAmPm7 getDay5() {
        return day5;
    }

    public void setDay6 (DayOfAmPm7 day6) {
        this.day6 = day6;
        dayList.add(day6);
    }
    public DayOfAmPm7 getDay6() {
        return day6;
    }

    public void setDay7 (DayOfAmPm7 day7) {
        this.day7 = day7;
        dayList.add(day7);
    }
    public DayOfAmPm7 getDay7() {
        return day7;
    }
}

