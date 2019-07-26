package com.example.realpilot.externalApiModel.kweatherShko;

import lombok.Getter;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "weather")
public class KweatherShkoTopModel {
    private AreaOfShko area;
    @Getter
    private List<AreaOfShko> areas =  new ArrayList<>();

    public void setArea (AreaOfShko area) {
        this.area = area;
        areas.add(area);
        this.area = new AreaOfShko();
    }
    public AreaOfShko getArea() {
        return area;
    }

}

