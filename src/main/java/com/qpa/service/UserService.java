package com.qpa.service;

import com.qpa.dto.LoginDTO;
import com.qpa.dto.RegisterDTO;
import com.qpa.entity.User;
import com.qpa.exception.ResourceNotFoundException;
import com.qpa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(RegisterDTO registerDTO) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Create new user
        User user = new User(
                registerDTO.getUsername(),
                registerDTO.getPassword(), // Note: In a real app, you'd hash the password
                registerDTO.getRoles()
        );

        return userRepository.save(user);
    }

    public User loginUser(LoginDTO loginDTO) {
        // Find user by username
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Simple password check (in a real app, use password encoding)
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return user;
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}