package com.haliri.israj.javaspringbootnats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	public static Logger getLogger(Object o) {
		return LoggerFactory.getLogger(o.getClass());
	}

}
