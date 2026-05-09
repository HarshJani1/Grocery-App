package com.grocery.api_gateway.Filters;

import com.grocery.api_gateway.Util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final RouteValidator validator;
    private final JwtUtil jwtUtil;

    public AuthenticationFilter(RouteValidator validator, JwtUtil jwtUtil) {
        this.validator = validator;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        ServerHttpRequest request = null;
        boolean secured = validator.isSecured.test(exchange.getRequest());

        log.info("Incoming request | method={} | path={} | isSecured={}", method, path, secured);

        // ALLOW preflight through without auth
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            log.debug("Allowing OPTIONS preflight request | path={}", path);
            return chain.filter(exchange);
        }

        if (secured) {
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                MDC.put("statusCode", String.valueOf(HttpStatus.UNAUTHORIZED.value()));
                log.warn("Missing Authorization header | method={} | path={}", method, path);
                MDC.clear();
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            }

            try {
                jwtUtil.validateToken(authHeader);
                String email = jwtUtil.extractEmail(authHeader);

                request = exchange.getRequest()
                        .mutate()
                        .header("Email", email)
                        .build();

                MDC.put("statusCode", String.valueOf(HttpStatus.OK.value()));
                log.info("Token validated successfully | method={} | path={} | email={}", method, path, email);
            } catch (Exception e) {
                MDC.put("statusCode", String.valueOf(HttpStatus.UNAUTHORIZED.value()));
                log.error("Invalid access token | method={} | path={} | error={}", method, path, e.getMessage(), e);
                MDC.clear();
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } finally {
                MDC.clear();
            }
        }

        return chain.filter(exchange.mutate().request(request).build());
    }
}
