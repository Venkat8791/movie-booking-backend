package com.mxmovies.common.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityUtils {

    private final JwtUtil jwtUtil;

    public SecurityUtils(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public UUID getCurrentUserId() {
        String token = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getCredentials();
        return jwtUtil.extractUserId(token);
    }

    public String getCurrentUserEmail() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
    }
}
