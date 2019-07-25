package com.example.realpilot.externalApiModel.kweatherAmPm7;

import lombok.Getter;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "weather")
public class KweatherAmPm7TopModel {
    private AreaOfAmPm7 area;
    @Getter
    private List<AreaOfAmPm7> areas =  new ArrayList<>();

    public void setArea (AreaOfAmPm7 area) {
        this.area = area;
        areas.add(area);
        this.area = new AreaOfAmPm7();
    }
    public AreaOfAmPm7 getArea() {
        return area;
    }

}
