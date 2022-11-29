package com.canvas.controllers.chromeApiServer;

import com.canvas.exceptions.handler.ControllerExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan(basePackages = {"com.canvas.controllers.chromeApiServer", "com.canvas.service"})
@Import(ControllerExceptionHandler.class)
public class ChromeApiServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChromeApiServerApplication.class, args);
	}
}
