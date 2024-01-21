package com.qkwl.service.capital;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
@ImportResource(locations = "classpath:edas-hsf.xml")
@MapperScan({"com.qkwl.service.common.mapper","com.qkwl.service.capital.dao"})
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
