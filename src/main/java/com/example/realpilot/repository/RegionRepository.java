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

    /*@Query("LOAD CSV WITH HEADERS FROM \"file:///region/KIKcd_H.20190701.CSV\" AS line " +
            "CREATE (r:Region {h_code: toInteger(line.h_code), si_do: line.si_do, si_gun_gu: line.si_gun_gu, eub_myeon_dong: line.eub_myeon_dong, created_date: line.created_date}) " +
            "RETURN r")
    Collection<Object> saveAllExcelData();
    */

    /*@Query("LOAD CSV WITH HEADERS FROM \"file:///region/KIKcd_H.20190701.CSV\" AS line\n" +
            "CALL apoc.do.when(line.si_gun_gu IS NOT NULL, " +
            "\"MERGE (siDo:Region {siDo: line.si_do}) MERGE (siDo)-[:UPPER_UNIT_OF]->(siGunGu:Region {siGunGu: line.si_gun_gu}) WITH line " +
            "CALL apoc.do.when(line.eub_myeon_dong IS NOT NULL, \\\"MATCH (siGunGu:Region {siGunGu: line.si_gun_gu}) " +
            "MERGE (siGunGu)-[:UPPER_UNIT_OF]->(eubMyeonDong:Region {eubMyeonDong: line.eub_myeon_dong})-[:REGION_NAME_OF]->(codeAndCreatedDate:Region {hCode: toInteger(line.h_code),createdDate: line.created_date})\\\", " +
            "\\\"MATCH (siGunGu:Region {siGunGu: line.si_gun_gu}) MERGE (siGunGu)-[:REGION_NAME_OF]->(codeAndCreatedDate:Region {hCode: toInteger(line.h_code),createdDate: line.created_date})\\\", {line: line}) YIELD value WITH value RETURN value\", " +
            "\"MERGE (siDo:Region {siDo: line.si_do}) MERGE (siDo)-[:REGION_NAME_OF]->(codeAndCreatedDate:Region {hCode: toInteger(line.h_code),createdDate: line.created_date})\", {line: line}) YIELD value WITH value RETURN value")
    Collection<Object> saveAllExcelData();
    */
}
