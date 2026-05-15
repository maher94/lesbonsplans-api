package com.lebonplan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LeBonPlanApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeBonPlanApplication.class, args);
    }
}
