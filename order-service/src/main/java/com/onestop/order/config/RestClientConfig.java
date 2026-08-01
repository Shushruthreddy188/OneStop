package com.onestop.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * HTTP clients the Order Service uses to coordinate checkout. Timeouts first per
 * the technology baseline; Resilience4j retries/circuit breakers come later.
 */
@Configuration
public class RestClientConfig {

    private static RestClient client(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(4));
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    @Bean
    public RestClient cartRestClient(
            @Value("${onestop.clients.cart.base-url:http://localhost:8083}") String baseUrl) {
        return client(baseUrl);
    }

    @Bean
    public RestClient catalogRestClient(
            @Value("${onestop.clients.catalog.base-url:http://localhost:8082}") String baseUrl) {
        return client(baseUrl);
    }

    @Bean
    public RestClient inventoryRestClient(
            @Value("${onestop.clients.inventory.base-url:http://localhost:8084}") String baseUrl) {
        return client(baseUrl);
    }

    @Bean
    public RestClient notificationRestClient(
            @Value("${onestop.clients.notification.base-url:http://localhost:8086}") String baseUrl) {
        return client(baseUrl);
    }
}
