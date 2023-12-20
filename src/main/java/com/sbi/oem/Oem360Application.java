package com.sbi.oem;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class Oem360Application extends SpringBootServletInitializer{

	public static void main(String[] args) {
		SpringApplication.run(Oem360Application.class, args);
	}

	@PostConstruct
	public void init(){
	    // Setting Spring Boot SetTimeZone
	    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
	//
}
