package com.giahung19.ecommerce_api.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;


@TestConfiguration(proxyBeanMethods = false)
public class PostgreSqlTestContainerConfig {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgreSqlContainer() {
        return new PostgreSQLContainer("postgres:16");
    }
}
