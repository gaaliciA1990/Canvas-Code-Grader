package com.canvas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "canvas")
@Configuration("application.properties")
public class CanvasConfiguration {
    private String hostUrl;

    private String authToken;

    @Bean
    public String getHostUrl() {
        return hostUrl;
    }

    @Bean
    public String getAuthToken() {
        return authToken;
    }

    public void setHostUrl(String hostUrl) {
        this.hostUrl = hostUrl;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
