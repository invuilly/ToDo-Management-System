package com.dmm.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.dmm.task.service.AccountUserDetailsService;

@SpringBootApplication
public class DmmApplication {
	
	@Autowired
	static AccountUserDetailsService userDetailsService;

	public static void main(String[] args) {
		SpringApplication.run(DmmApplication.class, args);
	}

}
