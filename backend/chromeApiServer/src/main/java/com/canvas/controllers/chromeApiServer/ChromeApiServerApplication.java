package com.canvas.controllers.chromeApiServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.canvas.controllers.chromeApiServer", "com.canvas.service"})
public class ChromeApiServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChromeApiServerApplication.class, args);
	}
}
