package com.example.realpilot.model.date;

import lombok.Data;

import java.io.Serializable;

@Data
public class Hour implements Serializable {
    private String uid;
    private Integer hour;
}
