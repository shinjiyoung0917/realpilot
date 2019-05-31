package com.example.realpilot.model;

import lombok.Builder;
import lombok.Data;
import org.neo4j.ogm.annotation.*;

import java.util.HashSet;
import java.util.Set;

@NodeEntity(label = "Sido")
@Data
//@Builder
public class Sido {
    @Property(name = "hCode")
    @Id
    private Long hCode;

    @Property(name = "name")
    @Index(unique = true)
    private String sidoName;

    @Property(name = "createdDate")
    private Integer createdDate;

    @Relationship(type = "UPPER_UNIT_OF")
    private Set<Sigungu> sigungus = new HashSet<>();

    public void addSigungu(Sigungu sigungu) {
        this.sigungus.add(sigungu);
    }

    public void removeSigungu() {
        this.sigungus.clear();
    }

}
