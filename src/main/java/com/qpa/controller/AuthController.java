package com.qpa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qpa.dto.LoginDTO;
import com.qpa.dto.RegisterDTO;
import com.qpa.dto.AuthResponse;
import com.qpa.entity.User;
import com.qpa.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterDTO registerDTO) {
        User user = authService.createUser(
            registerDTO.getUsername(),
            registerDTO.getPassword(),
            registerDTO.getRoles()
        );

        AuthResponse response = new AuthResponse(
            user.getId(),
            user.getUsername(),
            user.getRoles(),
            "User registered successfully"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(),
                loginDTO.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = authService.findByUsername(loginDTO.getUsername());
        
        AuthResponse response = new AuthResponse(
            user.getId(),
            user.getUsername(),
            user.getRoles(),
            "Login successful"
        );

        return ResponseEntity.ok(response);
    }
}
