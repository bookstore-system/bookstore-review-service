package com.hamtech.bookstorereviewservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * API Gateway validates JWT (RS256) and forwards identity via headers.
 */
@Component
@Profile("!test")
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_USER_ROLE = "X-User-Role";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_NAME = "X-User-Name";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String userId = request.getHeader(HEADER_USER_ID);
            if (StringUtils.hasText(userId)) {
                String role = request.getHeader(HEADER_USER_ROLE);
                String authority = "ROLE_USER";
                if (StringUtils.hasText(role)) {
                    authority = role.trim().toUpperCase();
                    if (!authority.startsWith("ROLE_")) {
                        authority = "ROLE_" + authority;
                    }
                }
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId.trim(), null, List.of(new SimpleGrantedAuthority(authority)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
