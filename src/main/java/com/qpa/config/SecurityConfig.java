package com.qpa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                //.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .csrf(csrf -> csrf.disable()) 
                .authorizeHttpRequests(auth -> {
                    // API endpoints authorization
                    auth.requestMatchers("/api/auth/**").permitAll();
                    auth.requestMatchers("/api/spots/evCharging").permitAll();
                    auth.requestMatchers("/api/spots/availability").permitAll();
                    auth.requestMatchers("api/spots/availableSpots").permitAll();
                    auth.requestMatchers(HttpMethod.GET, "/api/spots/statistics").hasRole("ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/spots/create").hasRole("SPOT_OWNER");
                    auth.requestMatchers(HttpMethod.PUT, "/api/spots/{spotId}").hasRole("SPOT_OWNER");
                    auth.requestMatchers(HttpMethod.GET, "/api/spots/owner").hasRole("SPOT_OWNER");
                    auth.requestMatchers(HttpMethod.DELETE, "/api/spots/{spotId}").hasAnyRole("SPOT_OWNER", "ADMIN");
                    auth.requestMatchers(HttpMethod.PATCH,"/api/spots/{spotId}/rate").hasRole("VEHICLE_OWNER");
                    auth.requestMatchers(HttpMethod.GET, "/api/spots/all").hasAnyRole("SPOT_OWNER", "VEHICLE_OWNER", "ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/spots/search").hasAnyRole("SPOT_OWNER", "VEHICLE_OWNER", "ADMIN");

                    // Static resources
                    auth.requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll();

                    // Public pages
                    auth.requestMatchers("/", "/login", "/register", "/error").permitAll();

                    // Web UI routes - matching your UIController
                    auth.requestMatchers("/home").authenticated();
                    auth.requestMatchers("/spots/create").hasRole("SPOT_OWNER");
                    auth.requestMatchers("/spots/list").authenticated();

                    // Default rule
                    auth.anyRequest().authenticated();
                })
                // For API clients
                .httpBasic(basic -> {})

                // For web browser clients
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}