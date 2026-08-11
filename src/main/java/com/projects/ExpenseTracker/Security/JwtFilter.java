package com.projects.ExpenseTracker.Security;

import com.projects.ExpenseTracker.Config.UserContextHolder;
import com.projects.ExpenseTracker.Entity.User;
import com.projects.ExpenseTracker.Repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getRequestURI();

            // 1. Skip filter for authentication routes
            if (!path.startsWith("/api/auth/")) {
                final String requestTokenHeader = request.getHeader("Authorization");

                // 2. Process Bearer token
                if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
                    String token = requestTokenHeader.substring(7);
                    String userIdentifier = jwtUtil.extractUsername(token);

                    if (userIdentifier != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                        User user = userRepository.findByEmail(userIdentifier)
                                .orElseThrow(() -> new RuntimeException("User not found with identifier: " + userIdentifier));

                        if (jwtUtil.validateToken(token, user)) {
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(
                                            user,
                                            null,
                                            Collections.emptyList());

                            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            SecurityContextHolder.getContext().setAuthentication(auth);

                            // Set thread-local context for service layer
                            UserContextHolder.setCurrentUserId(user.getId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("JWT Processing Error: {}", e.getMessage());
        } finally {
            try {
                filterChain.doFilter(request, response);
            } finally {
                UserContextHolder.clear();
            }
        }
    }
}