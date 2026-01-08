package com.example.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GitHubApiConfig {

    @Value("${github.api.token:}")
    private String githubToken;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            if (githubToken != null && !githubToken.isEmpty()) {
                requestTemplate.header("Authorization", "Bearer " + githubToken);
            }

            requestTemplate.header("Accept", "application/vnd.github.v3+json");
        };
    }
}
