package com.example.realpilot.externalApiModel.tmCoordinate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenModel {
    private List<TmCoordinate> list;
}
