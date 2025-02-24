package com.qpa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import com.qpa.dto.SpotSearchCriteria;
import com.qpa.entity.User;
import com.qpa.entity.UserRole;
import com.qpa.repository.UserRepository;
import com.qpa.service.SpotService;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class WebController {
    
    private SpotService spotService;
    private final UserRepository userRepository;
    
    @Autowired
    public WebController(SpotService spotService, UserRepository userRepository) {
        this.spotService = spotService;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SPOT_OWNER"))) {
                return "redirect:/spot-owner/dashboard";
            } else if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_VEHICLE_OWNER"))) {
                return "redirect:/vehicle-owner/dashboard";
            } else {
            	return "redirect:/admin/dashboard";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("roles", new UserRole[]{UserRole.ROLE_SPOT_OWNER, UserRole.ROLE_VEHICLE_OWNER});
        return "register";
    }

    @GetMapping("/spot-owner/dashboard")
    public String spotOwnerDashboard(Model model, Authentication authentication) {
        model.addAttribute("spots", spotService.getSpotByOwner(getCurrentUserId(authentication)));
        return "spot-owner/dashboard";
    }

    @GetMapping("/vehicle-owner/dashboard")
    public String vehicleOwnerDashboard(Model model) {
        model.addAttribute("searchCriteria", new SpotSearchCriteria());
        return "vehicle-owner/dashboard";
    }
    
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        model.addAttribute("statistics", spotService.getStatistics());
        model.addAttribute("spots", spotService.getAllSpots());
        return "admin/dashboard";
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("No authentication found");
        }
        
        return userRepository.findByUsername(authentication.getName())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
