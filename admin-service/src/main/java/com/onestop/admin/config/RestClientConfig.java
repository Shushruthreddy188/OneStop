package com.onestop.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    private static RestClient client(String baseUrl, String internalToken) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(4));
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory)
                .defaultHeader("X-Internal-Token", internalToken).build();
    }

    private final String internalToken;

    public RestClientConfig(@Value("${onestop.security.internal-token}") String internalToken) {
        this.internalToken = internalToken;
    }

    @Bean
    public RestClient catalogRestClient(
            @Value("${onestop.clients.catalog.base-url:http://localhost:8082}") String baseUrl) {
        return client(baseUrl, internalToken);
    }

    @Bean
    public RestClient orderRestClient(
            @Value("${onestop.clients.order.base-url:http://localhost:8085}") String baseUrl) {
        return client(baseUrl, internalToken);
    }

    @Bean
    public RestClient couponRestClient(
            @Value("${onestop.clients.coupon.base-url:http://localhost:8090}") String baseUrl) {
        return client(baseUrl, internalToken);
    }
}
