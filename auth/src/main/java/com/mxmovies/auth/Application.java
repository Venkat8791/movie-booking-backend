package com.mxmovies.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages  = "com.mxmovies")
@EntityScan(basePackages = "com.mxmovies")              // ← scans all JPA entities
@EnableJpaRepositories(basePackages = "com.mxmovies")   // ← scans all JPA repositories
@EnableMongoRepositories(basePackages = "com.mxmovies")
@EnableScheduling   // ← add this
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}