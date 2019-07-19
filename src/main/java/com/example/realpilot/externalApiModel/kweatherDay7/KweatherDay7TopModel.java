package com.example.realpilot.externalApiModel.kweatherDay7;

import lombok.Getter;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "weather")
public class KweatherDay7TopModel {
    private Area area;
    @Getter
    private List<Area> areas =  new ArrayList<>();

    public void setArea (Area area) {
        this.area = area;
        areas.add(area);
        this.area = new Area();
    }
    public Area getArea() {
        return area;
    }

}
