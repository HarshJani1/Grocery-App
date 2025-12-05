package com.grocery.api_gateway.Filters;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/auth/token",
            "/auth/register",
            "/auth/validate",
            "/auth/signup",
            "/service-auth/auth/register",
            "/service-auth/auth/token",
            "/service-auth/auth/signup",
            "/eureka"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().startsWith(uri));
}
