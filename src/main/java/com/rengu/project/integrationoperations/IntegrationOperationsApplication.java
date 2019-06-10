package com.rengu.project.integrationoperations;

import com.rengu.project.integrationoperations.util.disableWarnings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class IntegrationOperationsApplication {

    public static void main(String[] args) {
        disableWarnings.disableAccessWarnings();
        SpringApplication.run(IntegrationOperationsApplication.class, args);
    }
}
