package com.example.realpilot.repository;

import com.example.realpilot.model.Sigungu;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface SigunguRepository extends Neo4jRepository<Sigungu, String> {
    @Query("MATCH p=(:Eubmyeondong)<--(:Sigungu {name: \'종로구\'}) RETURN p")
    Iterable<Sigungu> findSggAndEmd();
    //Map<String, List<List<Map<String, Object>>>> findSggAndEmd();
}
