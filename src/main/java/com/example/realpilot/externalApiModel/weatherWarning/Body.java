package com.example.realpilot.externalApiModel.weatherWarning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Body {
    private Items items;

    @Override
    public String toString() {
        return "Body{" +
                " items='" + items + '\'' +
                '}';
    }

}
