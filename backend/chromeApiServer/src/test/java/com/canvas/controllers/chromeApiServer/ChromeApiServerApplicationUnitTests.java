package com.canvas.controllers.chromeApiServer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {"canvas.secretKey=someKey"})
class ChromeApiServerApplicationUnitTests {

	@Test
	void contextLoads() {
	}

}
