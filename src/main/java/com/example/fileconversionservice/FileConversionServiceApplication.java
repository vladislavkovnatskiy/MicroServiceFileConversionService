package com.example.fileconversionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FileConversionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileConversionServiceApplication.class, args);
	}

}
