package com.example.realpilot.repository;

import com.example.realpilot.model.Sido;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SidoRepository extends Neo4jRepository<Sido, String> {
}
