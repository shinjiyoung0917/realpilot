package com.example.realpilot.externalApiModel.weatherWarning;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenModel {
    private Response response;
}
