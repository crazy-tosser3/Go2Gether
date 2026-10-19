package com.example.STOLOTO;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class StolotoApplication {

	public static void main(String[] args) {
		SpringApplication.run(StolotoApplication.class, args);
	}

}
