package com.example.realpilot.model;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class Region {
    @Id
    @GeneratedValue
    private Long id;

    private Long hCode;
    private String siDo;
    private String siGunGu;
    private String eubMyeonDong;
    private Integer createdDate;


    //@Relationship // (type = "설정해줄 관계 이름")
    //private List<Weather> weathers = new ArrayList<> ();
    
}
