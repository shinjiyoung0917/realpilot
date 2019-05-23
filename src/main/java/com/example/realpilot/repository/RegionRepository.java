package com.example.realpilot.repository;

import com.example.realpilot.model.Region;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface RegionRepository extends Neo4jRepository<Region, Long> {
    @Query("MATCH (r:Region) RETURN COUNT(r)")
    Long findCountOfRegionNodes();

    @Query("LOAD CSV WITH HEADERS FROM \"file:///region/KIKcd_H.20190701.CSV\" AS line " +
            "CREATE (r:Region {h_code: toInteger(line.h_code), si_do: line.si_do, si_gun_gu: line.si_gun_gu, eub_myeon_dong: line.eub_myeon_dong, created_date: line.created_date}) " +
            "RETURN r")
    Collection<Object> saveAllExcelData();
}
