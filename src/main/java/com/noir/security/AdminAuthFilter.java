package com.noir.security;

import com.noir.config.AppConfig;
import com.noir.exception.AppException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class AdminAuthFilter implements Filter {

    @Autowired private TokenService tokenService;
    @Autowired private AppConfig appConfig;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        String path = request.getRequestURI();

        // Only protect non-login admin routes
        if (path.startsWith("/api/admin/") && !path.equals("/api/admin/login")) {
            String header = request.getHeader("Authorization");
            String token = (header != null && header.startsWith("Bearer "))
                    ? header.substring(7) : null;
            try {
                Map<String, Object> payload = tokenService.verify(
                        token, appConfig.getAdmin().getTokenSecret(), "Missing admin token");
                request.setAttribute("admin", payload);
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