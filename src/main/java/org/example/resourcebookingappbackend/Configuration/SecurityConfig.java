package org.example.resourcebookingappbackend.Configuration;

import org.example.resourcebookingappbackend.Service.CustomOAuthLoginService;
import org.example.resourcebookingappbackend.Service.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private JwtAuthenticationFilter jwtFilter;
    private CustomOAuthLoginService customOAuthLoginService;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter , CustomOAuthLoginService customOAuthLoginService) {
        this.customOAuthLoginService = customOAuthLoginService;
        this.jwtFilter = jwtFilter;
    }


    @Bean
    public SecurityFilterChain  securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/auth/**",
                                "/oauth2/**",
                                "/login/oauth2/**"
                                ).permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oAuthLogin ->oAuthLogin
                        .userInfoEndpoint(userInfo-> userInfo.userService(customOAuthLoginService))
                        .defaultSuccessUrl("/swagger-ui/index.html", true)
                )

                .addFilterBefore(jwtFilter , UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
}
