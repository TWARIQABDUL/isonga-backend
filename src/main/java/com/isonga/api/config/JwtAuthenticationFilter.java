package com.isonga.api.config;

import com.isonga.api.models.User;
import com.isonga.api.services.UserService;
import com.isonga.api.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

     @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth");
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. Get token from header
        //  System.out.println("➡️ JWT Filter triggered for: " + request.getRequestURI());
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Authorization header missing or invalid\"}");
            return;
        }

        String token = authHeader.substring(7); // Remove "Bearer "

        // 2. Validate token and extract email
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            return;
        }

        String email = jwtUtil.extractEmail(token);
        // System.out.println("extracted ");

        // 3. Check if user is not already authenticated
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<User> optionalUser = userService.findByEmail(email);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();

                // 4. Create authorities (Spring needs ROLE_ prefix!)
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                user, null, Collections.singletonList(authority));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // 5. Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
