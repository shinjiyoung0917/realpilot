package com.example.realpilot.model.date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
public class DateQueryResult {
    // root query
    private List<DataByFunc> monthsCount;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class DataByFunc {
        private Integer countOfMonths;
    }
}
