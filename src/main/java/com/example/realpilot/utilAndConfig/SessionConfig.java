package com.example.realpilot.utilAndConfig;

import org.neo4j.ogm.config.ClasspathConfigurationSource;
import org.neo4j.ogm.config.ConfigurationSource;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@ComponentScan("com.example.realpilot")
//@EnableTransactionManagement
@EnableNeo4jRepositories(basePackages = "com.example.realpilot.repository")
public class SessionConfig {
    @Bean
    public org.neo4j.ogm.config.Configuration configuration() {
        ConfigurationSource properties = new ClasspathConfigurationSource("ogm.properties");
        org.neo4j.ogm.config.Configuration configuration = new org.neo4j.ogm.config.Configuration.Builder(properties).build();
        return configuration;
    }

    @Bean
    public SessionFactory sessionFactory() {
        return new SessionFactory(configuration(), "com.example.realpilot.model"); // <packages> 도메인 모델 리스트가 들어가야함
    }

    @Bean
    public Neo4jTransactionManager transactionManager() {
        return new Neo4jTransactionManager(sessionFactory());
    }

    @Bean
    public DataSource dataSource() {
        String NEO4J_URL = System.getenv("jdbc:neo4j:http://localhost:11002?user=neo4j,password=1111");
        if (NEO4J_URL==null) NEO4J_URL=System.getProperty("NEO4J_URL","jdbc:neo4j:http://localhost:11002");
        return new DriverManagerDataSource(NEO4J_URL);
    }

}
