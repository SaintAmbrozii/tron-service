package com.example.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtTokenToUserIdHeaderFilter implements GlobalFilter, Ordered {

    private static final String USER_ID_HEADER_NAME = "X-USER-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> {
                    Authentication authentication = securityContext.getAuthentication();
                    if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                        String subject = jwt.getSubject();
                        return exchange.mutate()
                                .request(request ->
                                        request.headers(headers ->
                                                headers.set(USER_ID_HEADER_NAME, subject)))
                                .build();
                    }
                    return exchange;
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
