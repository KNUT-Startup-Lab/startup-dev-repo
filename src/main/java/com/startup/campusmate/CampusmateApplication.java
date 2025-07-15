package com.startup.campusmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CampusmateApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampusmateApplication.class, args);
	}

}
