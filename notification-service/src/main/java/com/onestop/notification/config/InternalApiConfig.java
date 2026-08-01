package com.onestop.notification.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Configuration
public class InternalApiConfig implements WebMvcConfigurer {
    private final String token;
    public InternalApiConfig(@Value("${onestop.security.internal-token}") String token) { this.token = token; }
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TokenInterceptor(token)).addPathPatterns("/internal/**");
    }
    private record TokenInterceptor(String expected) implements HandlerInterceptor {
        @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String supplied = request.getHeader("X-Internal-Token");
            boolean valid = supplied != null && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8));
            if (!valid) response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return valid;
        }
    }
}
