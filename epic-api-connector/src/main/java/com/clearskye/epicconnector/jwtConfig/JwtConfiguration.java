package com.clearskye.epicconnector.jwtConfig;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Configuration class for JSON Web Token (JWT) settings.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class JwtConfiguration {
    /**
     * JWT authentication filter for validating and processing JWTs.
     */
    private final JwtAuthFilter authFilter;

    /**
     * Configures the security filter chain.
     *
     * @param http The {@link HttpSecurity} to modify
     * @return The {@link SecurityFilterChain} that defines the security for the application
     * @throws Exception If an error occurs while configuring the security filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                ).sessionManagement(session -> {
                    session.sessionCreationPolicy(STATELESS);
                })
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}