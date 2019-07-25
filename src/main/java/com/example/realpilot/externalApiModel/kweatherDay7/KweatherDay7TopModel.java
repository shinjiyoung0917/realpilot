package com.example.realpilot.externalApiModel.kweatherDay7;

import lombok.Getter;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "weather")
public class KweatherDay7TopModel {
    private AreaOfDay7 area;
    @Getter
    private List<AreaOfDay7> areas =  new ArrayList<>();

    public void setArea (AreaOfDay7 area) {
        this.area = area;
        areas.add(area);
        this.area = new AreaOfDay7();
    }
    public AreaOfDay7 getArea() {
        return area;
    }

}
