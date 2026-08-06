package com.dzy.gateway.filter;

import com.dzy.gateway.util.GatewayJwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private final GatewayJwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /** 完全放行的路径（无需登录） */
    private static final List<String> WHITE_LIST = List.of(
            "/users/login",
            "/users/sms/send",
            "/users",
            "/merchant/apply",
            "/goods/search",
            "/goods/detail/**",
            "/goods/list",
            "/goods/sku/**",
            "/categories/tree",
            "/categories/*",
            "/reviews/product/**",
            "/payments/callback/**"
    );

    public AuthFilter(GatewayJwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        if (isWhiteListed(path, method)) {
            return chain.filter(exchange);
        }

        String token = extractToken(exchange);
        if (token == null || !jwtUtil.validateToken(token)) {
            return unauthorized(exchange, "请先登录");
        }

        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", userId.toString())
                .header("X-Username", username == null ? "" : username)
                .header("X-Role", role == null ? "" : role)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private boolean isWhiteListed(String path, HttpMethod method) {
        // 注册仅放行 POST /users
        if ("/users".equals(path) && method != HttpMethod.POST) {
            return false;
        }
        // 分类树公开读，写操作需登录
        if (path.startsWith("/categories") && method != HttpMethod.GET) {
            return false;
        }
        for (String pattern : WHITE_LIST) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return exchange.getRequest().getHeaders().getFirst("token");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(Map.of("code", 401, "msg", message));
            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        } catch (Exception e) {
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
