package com.qkwl.service.commission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
@ImportResource(locations = "classpath:edas-hsf.xml")
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
