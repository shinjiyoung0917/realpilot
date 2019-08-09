package com.example.realpilot.externalApiModel.kweatherWorld;

import lombok.Getter;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "weather")
public class KweatherWorldTopModel {
    private AreaOfWorld area;
    @Getter
    private List<AreaOfWorld> areas =  new ArrayList<>();

    public void setArea (AreaOfWorld area) {
        this.area = area;
        areas.add(area);
        this.area = new AreaOfWorld();
    }
    public AreaOfWorld getArea() {
        return area;
    }
}
