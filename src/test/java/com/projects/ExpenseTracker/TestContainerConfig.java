package com.projects.ExpenseTracker;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;


@TestConfiguration
public class TestContainerConfig {

    @Bean
    @ServiceConnection
    MySQLContainer<?> mysql(){
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.0"));
    }
}
