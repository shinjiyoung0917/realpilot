package com.example.realpilot.repository;


import com.example.realpilot.model.Region;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface RegionRepository extends Neo4jRepository<Region, Long> {
    Region findByHCode();
}
