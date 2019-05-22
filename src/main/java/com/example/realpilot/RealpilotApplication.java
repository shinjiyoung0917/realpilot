package com.example.realpilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EnableNeo4jRepositories(basePackages = "com.example.realpilot.repository")
public class RealpilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(RealpilotApplication.class, args);
    }

}
