package com.satellite.missions.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class SpaceCenterClientConfig {

    @Bean
    public RestClient spaceCenterRestClient(@Value("${app.server.base-url}") String baseUrl) {
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return RestClient.builder().baseUrl(normalized).build();
    }
}
