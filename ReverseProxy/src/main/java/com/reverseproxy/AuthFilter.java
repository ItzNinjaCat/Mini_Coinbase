package com.reverseproxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class AuthFilter implements GatewayFilter {

        @Value("${auth.url}")
        private String authUrl;

        @Value("${user.management.url}")
        private String userManagementUrl;

        @Autowired
        private RestTemplate restTemplate;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            MultiValueMap<String, HttpCookie> cookie = request.getCookies();
            if(!cookie.containsKey("Authorization")) {
                return onError(exchange, "No JWT token found", HttpStatus.UNAUTHORIZED);
            }

            String jwt = Objects.requireNonNull(cookie.getFirst("Authorization")).getValue();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + jwt);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity =
                    restTemplate.exchange( authUrl + "/auth/validate",
                            HttpMethod.GET, requestEntity, String.class);
            if (responseEntity.getStatusCode() != HttpStatus.OK ) {
                return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
            }
            ResponseEntity<String> responseEntity2 =
                    restTemplate.exchange( userManagementUrl + "/api/user/is_verified",
                            HttpMethod.GET, requestEntity, String.class);
            if (responseEntity2.getStatusCode() != HttpStatus.OK ) {
                return onError(exchange, "User is not verified", HttpStatus.UNAUTHORIZED);
            }
            return chain.filter(exchange);
        }

        public Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(httpStatus);
            return response.setComplete();
        }
}
