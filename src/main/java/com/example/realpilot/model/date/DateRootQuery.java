package com.example.realpilot.model.date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
public class DateRootQuery {
    private List<DataByFunc> monthsCount;
    private List<Dates> currentDate;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class DataByFunc {
        private Integer countOfMonths;
    }
}
