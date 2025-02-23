package com.qpa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/", "/login", "/register", "/api/auth/**").permitAll();
                auth.requestMatchers("/admin/**").hasRole("ADMIN");
                auth.requestMatchers("/spot-owner/**").hasRole("SPOT_OWNER");
                auth.requestMatchers("/vehicle-owner/**").hasRole("VEHICLE_OWNER");
                auth.anyRequest().authenticated();
            })
            .formLogin(form -> {
            	form.loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username") 
                .passwordParameter("password") 
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true");
            })
            .logout(logout -> {
                logout.logoutSuccessUrl("/login?logout=true");
                logout.permitAll();
            });
        
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
