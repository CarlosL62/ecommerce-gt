package com.ecommercegt.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtFilter;

    public SecurityConfig(JwtAuthFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        // Role-based access control can also be done at the method level using @PreAuthorize
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/moderation/**").hasRole("MODERATOR")
                        .requestMatchers("/api/orders/*/ship", "/api/orders/*/deliver").hasRole("LOGISTICS")
                        .requestMatchers("/api/products/**").hasRole("COMMON")
                        .requestMatchers("/api/cart/**").hasRole("COMMON")
                        // protected endpoints
                        .anyRequest().authenticated()
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173"); // Dev frontend
        config.addAllowedOrigin("https://ecommercegt-byme.netlify.app/"); // Production frontend
        config.addAllowedOrigin("https://georgann-restless-acquiescingly.ngrok-free.dev"); // Ngrok tunnel

        config.addAllowedHeader("*"); // Content-Type, Authorization, etc.
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS"); // Very important for CORS preflight requests
        config.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}