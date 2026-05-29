package com.noir.security;

import com.noir.exception.AppException;
import com.noir.model.StaffUser;
import com.noir.service.StaffAuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class KitchenAuthFilter implements Filter {

    @Autowired private StaffAuthService staffAuthService;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        String path = request.getRequestURI();

        // Login is public; everything else under /api/kitchen/ is protected
        if (path.startsWith("/api/kitchen/") && !path.equals("/api/kitchen/login")) {
            String header = request.getHeader("Authorization");
            String token = (header != null && header.startsWith("Bearer "))
                    ? header.substring(7) : null;
            try {
                StaffUser staff = staffAuthService.verifyKitchenToken(token);
                request.setAttribute("kitchenStaff", staff);
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
