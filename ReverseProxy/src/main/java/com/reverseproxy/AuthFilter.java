package com.reverseproxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects;
@Slf4j
@Component
public class AuthFilter implements GatewayFilter {

        private String authUrl = "http://auth-service:8090";

        private String userManagementUrl = "http://user-management-service:8081";

        private final RestTemplate restTemplate;

        public AuthFilter() {
            this.restTemplate = new RestTemplate();
        }
        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            log.info(String.valueOf(exchange));
            MultiValueMap<String, HttpCookie> cookie = request.getCookies();
            log.info("Cookies: " + cookie);
            if(!cookie.containsKey("jwt")) {
                return onError(exchange, "No JWT token found", HttpStatus.UNAUTHORIZED);
            }
            log.info("JWT token found: " + Objects.requireNonNull(cookie.getFirst("jwt")).getValue());
            String jwt = Objects.requireNonNull(cookie.getFirst("jwt")).getValue();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + jwt);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            log.info(authUrl + "/auth/validate");
            log.info(headers.toString());
            ResponseEntity<String> responseEntity =
                    restTemplate.exchange( authUrl + "/auth/validate",
                            HttpMethod.GET, requestEntity, String.class);
            log.info("Response from auth service: " + responseEntity.getStatusCode());
            if (responseEntity.getStatusCode() != HttpStatus.OK ) {
                return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
            }
            HttpHeaders headers2 = new HttpHeaders();
            headers2.add("Cookie", "jwt=" + jwt);
            HttpEntity<Void> requestEntity2 = new HttpEntity<>(headers2);
            ResponseEntity<String> responseEntity2 =
                    restTemplate.exchange( userManagementUrl + "/api/user/is_verified",
                            HttpMethod.GET, requestEntity2, String.class);
            log.info("Response from user management service: " + responseEntity2.getStatusCode());
            if (responseEntity2.getStatusCode() != HttpStatus.OK ) {
                return onError(exchange, "User is not verified", HttpStatus.UNAUTHORIZED);
            }
            log.info("User is verified");
            return chain.filter(exchange);
        }

        public Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(httpStatus);
            return response.setComplete();
        }
}
