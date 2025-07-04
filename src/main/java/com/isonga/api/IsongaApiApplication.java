package com.isonga.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IsongaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(IsongaApiApplication.class, args);
	}

}
