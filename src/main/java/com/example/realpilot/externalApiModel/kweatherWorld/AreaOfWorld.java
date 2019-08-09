package com.example.realpilot.externalApiModel.kweatherWorld;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@Getter
@XmlRootElement(name = "area")
public class AreaOfWorld {
    private DayOfWorld day0;
    private DayOfWorld day1;
    private DayOfWorld day2;
    private DayOfWorld day3;
    private DayOfWorld day4;
    private DayOfWorld day5;
    private DayOfWorld day6;
    private DayOfWorld day7;
    private List<DayOfWorld> dayList =  new ArrayList<>();

    @XmlAttribute
    private String code;
    @XmlAttribute
    private String reg_id;

    public void setDay0 (DayOfWorld day0) {
        this.day0 = day0;
        dayList.add(day0);
    }
    public DayOfWorld getDay0() {
        return day0;
    }

    public void setDay1 (DayOfWorld day1) {
        this.day1 = day1;
        dayList.add(day1);
    }
    public DayOfWorld getDay1() {
        return day1;
    }

    public void setDay2 (DayOfWorld day2) {
        this.day2 = day2;
        dayList.add(day2);
    }
    public DayOfWorld getDay2() {
        return day2;
    }

    public void setDay3 (DayOfWorld day3) {
        this.day3 = day3;
        dayList.add(day3);
    }
    public DayOfWorld getDay3() {
        return day3;
    }

    public void setDay4(DayOfWorld day4) {
        this.day4 = day4;
        dayList.add(day4);
    }
    public DayOfWorld getDay4() {
        return day4;
    }

    public void setDay5 (DayOfWorld day5) {
        this.day5 = day5;
        dayList.add(day5);
    }
    public DayOfWorld getDay5() {
        return day5;
    }

    public void setDay6 (DayOfWorld day6) {
        this.day6 = day6;
        dayList.add(day6);
    }
    public DayOfWorld getDay6() {
        return day6;
    }

    public void setDay7 (DayOfWorld day7) {
        this.day7 = day7;
        dayList.add(day7);
    }
    public DayOfWorld getDay7() {
        return day7;
    }
}
