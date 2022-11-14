package com.canvas.chromeApiServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
public class ChromeApiServerApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ChromeApiServerApplication.class, args);
	}

	@Override
	public void run(String... args) {
		new ChromeApiController();
	}

}
