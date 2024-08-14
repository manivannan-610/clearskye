package com.clearskye.epicconnector.jwtConfig;


import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.clearskye.epicconnector.utils.EpicConstants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Filter that handles JWT authentication for incoming requests.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    /**
     * JWT service instance for handling JSON Web Token operations.
     */
    private final JwtService jwtService;
    /**
     * Logger instance for logging JwtAuthFilter events.
     */
    private static final Logger logger = LogManager.getLogger(JwtAuthFilter.class);

    /**
     * Filters each request to check for a valid JWT and sets the authentication context.
     *
     * @param request     The HTTP request
     * @param response    The HTTP response
     * @param filterChain The filter chain
     * @throws ServletException If an error occurs during filtering
     * @throws IOException      If an IO error occurs during filtering
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(EpicConstants.AUTHORIZATION);
        String token = null;
        String username = null;
        try {
            if (authHeader != null && authHeader.startsWith(EpicConstants.BEARER)) {
                token = authHeader.substring(7);
                username = jwtService.extractUsername(token, EpicConstants.ACCESS_TOKEN_TYPE);
            }
            if (username != null && jwtService.validateToken(token, EpicConstants.ACCESS_TOKEN_TYPE) && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception ex) {
            logger.error(MessageFormat.format("Un authorized access. because of error {0} : ", ex.getMessage()));
            response.getWriter().print("Un Authorized access.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }
}