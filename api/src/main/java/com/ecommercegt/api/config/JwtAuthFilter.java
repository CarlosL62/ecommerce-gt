package com.ecommercegt.api.config;

import com.ecommercegt.api.repository.UserRepository;
import com.ecommercegt.api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UserRepository users;

    public JwtAuthFilter(JwtService jwt, UserRepository users) {
        this.jwt = jwt; this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                String email = jwt.getSubject(token);
                var user = users.findByEmail(email).orElse(null);
                if (user != null) {
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
                    var authToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception ignored) {
                // Expired or invalid token
            }
        }
        chain.doFilter(req, res);
    }
}