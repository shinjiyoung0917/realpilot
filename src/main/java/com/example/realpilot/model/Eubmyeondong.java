package com.example.realpilot.model;

import lombok.Builder;
import lombok.Data;
import org.neo4j.ogm.annotation.*;

@NodeEntity(label = "Eubmyeondong")
@Data
//@Builder
public class Eubmyeondong {
    @Property(name = "hCode")
    @Id
    private Long hCode;

    @Property(name = "name")
    @Index(unique = true)
    private String eubmyeondongName;

    @Property(name = "createdDate")
    private Integer createdDate;
}
