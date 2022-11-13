package com.canvas.chromeApiServer;

import com.canvas.config.CanvasConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CanvasConfiguration.class)
public class ChromeApiServerApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ChromeApiServerApplication.class, args);
	}

	@Override
	public void run(String... args) {
		new ChromeApiController();
	}

}
