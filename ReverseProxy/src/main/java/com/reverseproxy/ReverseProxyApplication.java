package com.reverseproxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication

public class ReverseProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReverseProxyApplication.class, args);
    }

    @Value("${user.management.url}")
    private String userManagementUrl;

    @Value("${transaction.url}")
    private String transactionUrl;

    @Value("${crypto.url}")
    private String cryptoUrl;

    @Value("${fiat.url}")
    private String fiatUrl;

    @Value("${auth.url}")
    private String authUrl;


    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("users", r -> r.path("/api/user/**")
                        .uri(userManagementUrl))
                .route("transactions", r -> r.path("/transaction/**")
                        .filters(f -> f.filter(new AuthFilter()))
                        .uri(transactionUrl))
                .route("crypto", r -> r.path("/api/crypto/**")
                        .filters(f -> f.filter(new AuthFilter()))
                        .uri(cryptoUrl))
                .route("fiat", r -> r.path("/api/fiat/**")
                        .filters(f -> f.filter(new AuthFilter()))
                        .uri(fiatUrl))
                .route("auth", r -> r.path("/auth/**")
                        .uri(authUrl))
                .build();
    }

}
