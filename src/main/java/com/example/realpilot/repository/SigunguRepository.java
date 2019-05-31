package com.example.realpilot.repository;

import com.example.realpilot.model.Sigungu;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface SigunguRepository extends Neo4jRepository<Sigungu, String> {
}
