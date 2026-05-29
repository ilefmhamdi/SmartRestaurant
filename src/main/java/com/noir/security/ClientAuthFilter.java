package com.noir.security;

import com.noir.exception.AppException;
import com.noir.model.User;
import com.noir.service.ClientAuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ClientAuthFilter implements Filter {

    @Autowired private ClientAuthService clientAuthService;

    private static final java.util.Set<String> PROTECTED = java.util.Set.of(
        "/api/auth/me",
        "/api/client"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        String path = request.getRequestURI();

        boolean isProtected = PROTECTED.contains(path) || path.startsWith("/api/client/");

        if (isProtected) {
            String header = request.getHeader("Authorization");
            String token = (header != null && header.startsWith("Bearer "))
                    ? header.substring(7) : null;
            try {
                User user = clientAuthService.getCurrentUser(token);
                request.setAttribute("client", user);
            } catch (AppException e) {
                HttpServletResponse response = (HttpServletResponse) res;
                response.setStatus(e.getStatusCode());
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"" + e.getMessage() + "\",\"details\":null}");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}