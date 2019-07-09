package com.example.realpilot.model.region;

import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Sigungu {
    private String uid;
    private Long hCode;
    private String sigunguName;
    private Integer createdDate;
    private Integer gridX;
    private Integer gridY;
    private List<Eubmyeondong> eubmyeondongs;

    public void addEubmyeondong(Eubmyeondong eubmyeondong) {
        this.eubmyeondongs.add(eubmyeondong);
    }

}
