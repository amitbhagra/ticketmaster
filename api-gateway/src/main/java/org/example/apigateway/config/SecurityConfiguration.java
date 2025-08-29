package org.example.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Value("${spring.security.enabled:true}")
    private boolean securityEnabled;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.cors(cors -> {})
                .csrf(ServerHttpSecurity.CsrfSpec::disable);

        if (!securityEnabled) {
            http.authorizeExchange(exchanges -> exchanges.anyExchange().permitAll());
        } else {
            http.authorizeExchange(exchanges -> exchanges.
                    pathMatchers("/api/v1/eventslist/**").permitAll()
                            .pathMatchers("/api/v1/bookings/**").hasAuthority("ROLE_user").
                            anyExchange().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2
                            .jwt(jwt -> jwt
                                    .jwtAuthenticationConverter(jwtAuthenticationConverter(keycloakRoleConverter()))
                            )
                    );
        }
        return http.build();
    }

    @Bean
    public Converter<Jwt, Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter(KeycloakRoleConverter keycloakRoleConverter) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(keycloakRoleConverter);
        return jwt -> Mono.just(converter.convert(jwt));
    }

    @Bean
    public KeycloakRoleConverter keycloakRoleConverter() {
        return new KeycloakRoleConverter();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*")); // Allows all origins with credentials
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            if (securityEnabled) {
                return exchange.getPrincipal()
                        .cast(JwtAuthenticationToken.class)
                        .map(token -> {
                            Jwt jwt = token.getToken();
                            String username = jwt.getClaimAsString("preferred_username");
                            return username != null ? username : "anonymous";
                        });
            } else {
                return Mono.just(
                        exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                );
            }
        };
    }
}
