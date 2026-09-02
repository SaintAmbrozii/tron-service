package com.example.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Flux;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchange ->
                        exchange
                                .pathMatchers("/actuator/**").permitAll()
                                // юзеры
                                .pathMatchers(HttpMethod.POST, "/api/v1/admin/users/list").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.GET, "/api/v1/admin/users/**").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.POST, "/api/v1/users/register").hasAnyRole("ADMIN","USER")
                                .pathMatchers(HttpMethod.GET, "/api/v1/users/profile").hasAnyRole("ADMIN","USER")
                                .pathMatchers(HttpMethod.PATCH, "/api/v1/users/profile").hasAnyRole("ADMIN","USER")
                                .pathMatchers(HttpMethod.DELETE, "/api/v1/users/profile").hasAnyRole("ADMIN","USER")
                                // обменник
                                .pathMatchers(HttpMethod.GET, "/api/v1/exchange/toRub").hasAnyRole("USER","ADMIN")
                                .pathMatchers(HttpMethod.POST, "/api/v1/exchange/toRub").hasAnyRole("USER","ADMIN")
                                // платежки
                                .pathMatchers(HttpMethod.POST, "/api/v1/payments/**").hasRole("ADMIN")
                                .pathMatchers(HttpMethod.GET, "/api/v1/payments/**").hasRole("ADMIN")
                                // уведомления
                                .pathMatchers(HttpMethod.GET, "/api/v1/notifications/**").hasRole("ADMIN")

                                .anyExchange().authenticated())
                .oauth2ResourceServer(customizer -> customizer.jwt(Customizer.withDefaults()))
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }


    @Bean
    public ReactiveJwtAuthenticationConverter authenticationConverter(Converter<Jwt, Flux<GrantedAuthority>> authoritiesConverter) {
        final var authenticationConverter = new ReactiveJwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        authenticationConverter.setPrincipalClaimName(StandardClaimNames.PREFERRED_USERNAME);
        return authenticationConverter;
    }
}
