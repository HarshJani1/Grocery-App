package com.grocery.api_gateway.config;

import com.grocery.api_gateway.Util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

@Configuration
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    private final JwtUtil jwtUtil;

    public AppConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public RestTemplate template() {
        return new RestTemplate();
    }

    /**
     * Rate-limit key resolver.
     *
     * Priority:
     *   1. Authenticated users  → "rate-limit:user:<email>"   (extracted from JWT)
     *   2. Anonymous / preflight → "rate-limit:ip:<ip-address>"
     *
     * This ensures each user has their own independent token bucket,
     * and unauthenticated traffic (login, register) is bucketed per IP.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // 1. Try to resolve key from JWT Bearer token
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    String email = jwtUtil.extractEmail(token);
                    if (email != null && !email.isBlank()) {
                        log.debug("Rate-limit key resolved from JWT | key=rate-limit:user:{}", email);
                        return Mono.just("rate-limit:user:" + email);
                    }
                } catch (Exception e) {
                    // Token is malformed / expired — AuthenticationFilter will reject it;
                    // fall through to IP-based limiting so this request is still counted.
                    log.debug("Could not extract email from token for rate-limit key, falling back to IP | error={}", e.getMessage());
                }
            }

            // 2. Fallback — use client IP address
            String ip = (exchange.getRequest().getRemoteAddress() != null)
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            log.debug("Rate-limit key resolved from IP | key=rate-limit:ip:{}", ip);
            return Mono.just("rate-limit:ip:" + ip);
        };
    }
}
