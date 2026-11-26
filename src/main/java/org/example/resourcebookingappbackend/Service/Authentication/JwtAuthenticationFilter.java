package org.example.resourcebookingappbackend.Service.Authentication;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Service
public class JwtAuthenticationFilter  extends OncePerRequestFilter {

    private CustomUserDetailService customUserDetailService;
    private JwtService jwtService;

    public JwtAuthenticationFilter(CustomUserDetailService customUserDetailService, JwtService jwtService) {
        this.customUserDetailService = customUserDetailService;
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return path.equals("/api/auth/register")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/verify-email")
                || path.equals("/api/auth/resend-verification")
                || path.equals("/api/auth/forgot-password")
                || path.equals("/api/auth/reset-password")
                || path.equals("/api/auth/google")
                || path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || path.startsWith("/swagger-ui/")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();
        if (token.isBlank()) {
            sendUnauthorized(response);
            return;
        }
        String userName = jwtService.extractUsername(token);
        if (userName == null || userName.isBlank()) {
            sendUnauthorized(response);
            return;
        }

        UserDetails userDetails = customUserDetailService.loadUserByUsername(userName);
        if(jwtService.isTokenValid(token, userDetails.getUsername())) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        }else{
            sendUnauthorized(response);
            return;
        }

        filterChain.doFilter(request, response);

    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                """
                {
                  "status": 401,
                  "error": "Unauthorized",
                  "message": "Invalid or expired authentication token"
                }
                """
        );
    }
}
