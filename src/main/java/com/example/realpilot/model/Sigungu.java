package com.example.realpilot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import org.neo4j.ogm.annotation.*;

import java.util.HashSet;
import java.util.Set;

@NodeEntity(label = "Sigungu")
@Data
//@Builder
public class Sigungu {
    @Property(name = "hCode")
    @Id
    private Long hCode;

    @Property(name = "name")
    @Index(unique = true)
    private String sigunguName;

    @Property(name = "createdDate")
    private Integer createdDate;

    @Relationship(type = "UPPER_UNIT_OF")
    private Set<Eubmyeondong> eubmyeondongs = new HashSet<>();

    /*@JsonIgnoreProperties("sigungu")
    @Relationship(type = "UPPER_UNIT_OF", direction = Relationship.INCOMING)
    */

    public void addEubmyeondong(Eubmyeondong eubmyeondong) {
        this.eubmyeondongs.add(eubmyeondong);
    }

}
