package com.ariche.boatapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.ariche.boatapi.entity")
@EnableJpaRepositories("com.ariche.boatapi.repository")
public class MyBoatApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyBoatApplication.class, args);
    }

}
