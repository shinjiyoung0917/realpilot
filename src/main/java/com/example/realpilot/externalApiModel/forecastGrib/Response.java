package com.example.realpilot.externalApiModel.forecastGrib;

import com.example.realpilot.externalApiModel.weatherWarning.Body;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {
    private Body body;
}
